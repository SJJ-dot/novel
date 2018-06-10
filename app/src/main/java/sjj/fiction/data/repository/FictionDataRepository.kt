package sjj.fiction.data.repository

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import sjj.fiction.data.source.DataSourceInterface
import sjj.fiction.data.source.local.LocalFictionDataSource
import sjj.fiction.data.source.remote.aszw.AszwFictionDataSource
import sjj.fiction.data.source.remote.biquge.BiqugeDataSource
import sjj.fiction.data.source.remote.dhzw.DhzwDataSource
import sjj.fiction.data.source.remote.yunlaige.YunlaigeDataSource
import sjj.fiction.model.Book
import sjj.fiction.model.BookGroup
import sjj.fiction.model.Chapter
import sjj.fiction.model.Event
import sjj.fiction.util.bus
import sjj.fiction.util.domain
import sjj.fiction.util.observableCreate

val fictionDataRepository by lazy { FictionDataRepository() }
/**
 * Created by SJJ on 2017/9/2.
 */
class FictionDataRepository {
    private val sources = mutableMapOf<String, FictionDataRepository.RemoteSource>()

    private val localSource = LocalFictionDataSource()

    init {
        val input: (RemoteSource) -> Unit = { sources[it.domain()] = it }
        input(DhzwDataSource())
        input(YunlaigeDataSource())
        input(AszwFictionDataSource())
        input(BiqugeDataSource())
    }

    fun search(search: String): Observable<List<BookGroup>> = observableCreate<List<BookGroup>> { emitter ->
        if (search.isEmpty()) {
            throw IllegalArgumentException("搜索内容不能为空")
        }
        val map = mutableMapOf<String, BookGroup>()
        val count = object {
            var count = sources.size
            var complete = false
            @Synchronized
            fun complete() {
                count--
                complete = true
                emitter.onNext(map.values.toList())
                if (count == 0) {
                    emitter.onComplete()
                }
            }

            @Synchronized
            fun error(throwable: Throwable) {
                count--
                emitter.onNext(map.values.toList())
                if (count == 0) {
                    if (complete) {
                        emitter.onComplete()
                    } else emitter.onError(throwable)
                }
            }

        }
        val com = CompositeDisposable()
        sources.forEach {
            com.add(it.value.search(search).subscribe({
                it.forEach {
                    map.getOrPut(it.name + it.author, { BookGroup(it) }).books.add(it)
                }
                if (emitter.isDisposed) com.dispose() else count.complete()
            }, {
                if (emitter.isDisposed) com.dispose() else count.error(it)
            }))
        }
    }

    fun loadBookDetailsAndChapter(book: BookGroup, force: Boolean = false): Observable<BookGroup> = Observable.create { emitter ->
        val com = CompositeDisposable()
        val remote = {
            val disposable = sources[book.currentBook.url.domain()]
                    ?.loadBookDetailsAndChapter(book.currentBook)
                    ?.flatMap {
                        bus.onNext(Event(Event.NEW_BOOK, book))
                        localSource.saveBookGroup(listOf(book))
                    }
                    ?.map { book }
                    ?.subscribe({
                        if (emitter.isDisposed) com.dispose() else emitter.onNext(book)
                        if (emitter.isDisposed) com.dispose() else emitter.onComplete()
                    }, {
                        if (emitter.isDisposed) com.dispose() else emitter.onError(it)
                    })
            if (disposable == null) {
                emitter.tryOnError(Exception("未知源 ${book.currentBook.url}"))
            } else {
                com.add(disposable)
            }

        }

        if (!force) {
            com.add(localSource.loadBookDetailsAndChapter(book.currentBook).subscribe({
                book.currentBook = it
                if (emitter.isDisposed) com.dispose() else emitter.onNext(book)
                if (emitter.isDisposed) com.dispose() else emitter.onComplete()
            }, {
                remote()
            }))
        } else {
            remote()
        }
    }


    fun loadBookChapter(chapter: Chapter): Observable<Chapter> = Observable.create<Chapter> { emitter ->
        val com = CompositeDisposable()
        com.add(localSource.loadBookChapter(chapter).subscribe({
            if (!it.isLoadSuccess) throw Exception("not load")
            if (emitter.isDisposed) com.dispose() else emitter.onNext(it)
            if (emitter.isDisposed) com.dispose() else emitter.onComplete()
        }, {
            if (emitter.isDisposed) com.dispose()
            else
                com.add((sources[chapter.url.domain()]
                        ?.loadBookChapter(chapter)
                        ?.flatMap {
                            localSource.saveChapter(it)
                        }
                        ?: error("未知源 ${chapter.url}"))
                        .subscribe({
                            if (emitter.isDisposed) com.dispose() else emitter.onNext(it)
                            if (emitter.isDisposed) com.dispose() else emitter.onComplete()
                        }, {
                            if (emitter.isDisposed) com.dispose() else emitter.onError(it)
                        }))
        }))
    }

    fun loadBookGroups(): Observable<List<BookGroup>> = localSource.loadBookGroups()

    fun loadBookGroup(bookName: String, author: String): Observable<BookGroup> = localSource.loadBookGroup(bookName, author)

    fun saveBookGroup(book: List<BookGroup>): Observable<List<BookGroup>> {
        return localSource.saveBookGroup(book)
    }

    fun cachedBookChapter(book: Book): Flowable<Book> = Flowable.create({ emitter ->
        val count = object {
            var count = book.chapterList.size
            var complete = false
            @Synchronized
            fun complete() {
                count--
                complete = true
                if (count == 0) emitter.onComplete()
            }

            @Synchronized
            fun error(throwable: Throwable) {
                count--
                if (count == 0) {
                    if (complete) emitter.onComplete()
                    else emitter.onError(throwable)
                }
            }

        }
        val com = CompositeDisposable()
        book.chapterList.forEach {
            com.add(loadBookChapter(it).subscribe({
                if (emitter.isCancelled) com.dispose() else emitter.onNext(book)
            }, {
                if (emitter.isCancelled) com.dispose() else count.error(it)
            }, {
                if (emitter.isCancelled) com.dispose() else count.complete()
            }))
        }
    }, BackpressureStrategy.LATEST)

    fun deleteBookGroup(bookName: String, author: String) = localSource.deleteBookGroup(bookName, author)


    interface RemoteSource : Base {
        fun domain(): String
        fun search(search: String): Observable<List<Book>>

    }

    interface SourceLocal : Base {
        fun saveBookGroup(book: List<BookGroup>): Observable<List<BookGroup>>
        fun updateBookGroup(book: BookGroup): Observable<BookGroup>
        fun updateBook(book: Book): Observable<Book>
        fun saveChapter(chapter: Chapter): Observable<Chapter>
        fun loadBookGroups(): Observable<List<BookGroup>>
        fun loadBookGroup(bookName: String, author: String): Observable<BookGroup>
        fun deleteBookGroup(bookName: String, author: String): Observable<BookGroup>
    }

    interface Base : DataSourceInterface {
        fun loadBookChapter(chapter: Chapter): Observable<Chapter>
        fun loadBookDetailsAndChapter(book: Book): Observable<Book>

    }
}
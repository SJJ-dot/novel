package sjj.fiction.data.Repository.impl

import com.raizlabs.android.dbflow.kotlinextensions.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import sjj.alog.Log
import sjj.fiction.data.Repository.FictionDataRepository
import sjj.fiction.data.source.local.LocalFictionDataSource
import sjj.fiction.data.source.remote.dhzw.DhzwDataSource
import sjj.fiction.data.source.remote.yunlaige.YunlaigeDataSource
import sjj.fiction.model.Book
import sjj.fiction.model.BookGroup
import sjj.fiction.model.BookGroup_Table
import sjj.fiction.model.Chapter
import sjj.fiction.util.def
import sjj.fiction.util.domain
import sjj.fiction.util.observableCreate

/**
 * Created by SJJ on 2017/9/3.
 */
class FictionDataRepositoryImpl : FictionDataRepository {
    private val sources = mutableMapOf<String, FictionDataRepository.RemoteSource>()

    private val localSource = LocalFictionDataSource()

    init {
        val dh: FictionDataRepository.RemoteSource = DhzwDataSource()
        sources[dh.domain()] = dh
        val yu: FictionDataRepository.RemoteSource = YunlaigeDataSource()
        sources[yu.domain()] = yu
    }

    override fun search(search: String): Observable<List<BookGroup>> = observableCreate<List<BookGroup>> { emitter ->
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

    override fun getSearchHistory(): Observable<List<String>> = localSource.getSearchHistory()

    override fun setSearchHistory(value: List<String>): Observable<List<String>> = localSource.setSearchHistory(value)
    override fun loadBookDetailsAndChapter(book: BookGroup, force: Boolean): Observable<BookGroup> = observableCreate { emitter ->
        val com = CompositeDisposable()
        val remote= {
            com.add((sources[book.currentBook.url.domain()]
                    ?.loadBookDetailsAndChapter(book.currentBook)
                    ?.flatMap { localSource.saveBookGroup(listOf(book)) }
                    ?.map { book }
                    ?: error("未知源 ${book.currentBook.url}"))
                    .subscribe({
                        if (emitter.isDisposed) com.dispose() else emitter.onNext(book)
                        if (emitter.isDisposed) com.dispose() else emitter.onComplete()
                    }, {
                        if (emitter.isDisposed) com.dispose() else emitter.onError(it)
                    }))
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
    override fun loadBookChapter(chapter: Chapter): Observable<Chapter> = Observable.create<Chapter> { emitter ->
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

    override fun loadBookGroups(): Observable<List<BookGroup>> = localSource.loadBookGroups()

    override fun loadBookGroup(bookName: String, author: String): Observable<BookGroup> = localSource.loadBookGroup(bookName, author)

    override fun saveBookGroup(book: List<BookGroup>): Observable<List<BookGroup>> {
        return localSource.saveBookGroup(book)
    }
    override fun cachedBookChapter(book: Book): Observable<Book> = Observable.create<Book> { emitter ->
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
                if (emitter.isDisposed) com.dispose() else emitter.onNext(book)
            }, {
                if (emitter.isDisposed) com.dispose() else count.error(it)
            }, {
                if (emitter.isDisposed) com.dispose() else count.complete()
            }))
        }
    }

    override fun deleteBookGroup(bookName: String, author: String) = localSource.deleteBookGroup(bookName, author)
}

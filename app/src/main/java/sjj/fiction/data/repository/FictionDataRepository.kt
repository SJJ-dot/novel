package sjj.fiction.data.repository

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import sjj.fiction.data.source.local.LocalFictionDataSource
import sjj.fiction.data.source.remote.aszw.AszwFictionDataSource
import sjj.fiction.data.source.remote.biquge.BiqugeDataSource
import sjj.fiction.data.source.remote.dhzw.DhzwDataSource
import sjj.fiction.data.source.remote.yunlaige.YunlaigeDataSource
import sjj.fiction.model.Book
import sjj.fiction.model.BookSourceRecord
import sjj.fiction.model.Chapter
import sjj.fiction.util.domain
import sjj.fiction.util.observableCreate

val fictionDataRepository by lazy { FictionDataRepository() }

/**
 * Created by SJJ on 2017/9/2.
 */
class FictionDataRepository {
    private val sources = mutableMapOf<String, FictionDataRepository.RemoteSource>()

    private val localSource: LocalSource = LocalFictionDataSource()

    init {
        val input: (RemoteSource) -> Unit = { sources[it.domain()] = it }
        input(DhzwDataSource())
        input(YunlaigeDataSource())
        input(AszwFictionDataSource())
        input(BiqugeDataSource())
    }

    fun search(search: String): Observable<List<Pair<BookSourceRecord, List<Book>>>> = observableCreate { emitter ->
        if (search.isEmpty()) {
            throw IllegalArgumentException("搜索内容不能为空")
        }
        val map = mutableMapOf<String, MutableList<Book>>()
        val count = object {
            var count = sources.size
            var complete = false
            @Synchronized
            fun complete() {
                count--
                complete = true

                emitter.onNext(map.map { entry ->
                    Pair(BookSourceRecord().apply {
                        val first = entry.value.first()
                        bookName = first.name
                        author = first.author
                        bookUrl = first.url
                    }, entry.value)
                })
                if (count == 0) {
                    emitter.onComplete()
                }
            }

            @Synchronized
            fun error(throwable: Throwable) {
                count--
                emitter.onNext(map.map { entry ->
                    Pair(BookSourceRecord().apply {
                        val first = entry.value.first()
                        bookName = first.name
                        author = first.author
                        bookUrl = first.url
                    }, entry.value)
                })
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
                    map.getOrPut(it.name + it.author, { mutableListOf() }).add(it)
                }
                if (emitter.isDisposed) com.dispose() else count.complete()
            }, {
                if (emitter.isDisposed) com.dispose() else count.error(it)
            }))
        }
    }

    fun getBook(url: String): Flowable<Book> {
//      return  localSource.getBook(url)
        return localSource.getBook(url).doOnNext {
            refreshBook(url).subscribe()
        }
    }

    fun refreshBook(url: String): Observable<Book> {
        return Observable.just(url).flatMap {
            sources[it.domain()]?.getBook(it) ?: throw Exception("未知源 $it")
        }.flatMap(localSource::saveBook)
    }

    fun loadBookChapter(url: String): Observable<Chapter> {
        return localSource.getChapter(url).flatMap {
            if (it.isLoadSuccess) {
                Observable.just(it)
            } else {
                sources[url.domain()]!!.getChapter(url).flatMap(localSource::saveChapter)
            }
        }
    }

    fun getBooks(): Flowable<List<Book>> = localSource.getAllReadingBook()


    fun cachedBookChapter(book: Book): Flowable<Chapter> {
        return Observable.fromIterable(book.chapterList).map {
            it.url
        }.flatMap {
            sources[it.domain()]!!.getChapter(it).flatMap(localSource::saveChapter)
        }.toFlowable(BackpressureStrategy.LATEST)
    }

    fun deleteBook(bookName: String, author: String) = localSource.deleteBook(bookName, author)


    interface RemoteSource {
        fun getChapter(url: String): Observable<Chapter>
        fun getBook(url: String): Observable<Book>
        fun domain(): String
        fun search(search: String): Observable<List<Book>>
    }

    interface LocalSource {
        fun getBook(url: String): Flowable<Book>
        fun getChapter(url: String): Observable<Chapter>
        fun saveChapter(chapter: Chapter): Observable<Chapter>
        fun saveBook(book: Book): Observable<Book>
        fun getAllReadingBook(): Flowable<List<Book>>
        fun saveBooks(books: List<Pair<BookSourceRecord, List<Book>>>): Observable<List<Book>>
        fun deleteBook(bookName: String, author: String):Observable<String>
    }

}
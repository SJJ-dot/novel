package sjj.fiction.data.repository

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import sjj.fiction.data.source.local.LocalFictionDataSource
import sjj.fiction.data.source.remote.aszw.AszwFictionDataSource
import sjj.fiction.data.source.remote.biquge.BiqugeDataSource
import sjj.fiction.data.source.remote.dhzw.DhzwDataSource
import sjj.fiction.data.source.remote.yunlaige.YunlaigeDataSource
import sjj.fiction.model.Book
import sjj.fiction.model.BookSourceRecord
import sjj.fiction.model.Chapter
import sjj.fiction.util.domain

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

    fun search(search: String): Single<List<Pair<BookSourceRecord, List<Book>>>> {
        return Observable.fromIterable(sources.values).flatMap {
            it.search(search).onErrorResumeNext(Observable.empty())
        }.reduce(mutableMapOf<String, MutableList<Book>>(), { map, bs ->
            bs.forEach {
                map.getOrPut(it.name + it.author, { mutableListOf() }).add(it)
            }
            map
        }).map {
            it.map { entry ->
                Pair(BookSourceRecord().apply {
                    val first = entry.value.first()
                    bookName = first.name
                    author = first.author
                    bookUrl = first.url
                }, entry.value)
            }
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
                sources[url.domain()]!!.getChapterContent(url).flatMap(localSource::saveChapter)
            }
        }
    }

    fun getBooks(): Flowable<List<Book>> = localSource.getAllReadingBook()


    fun cachedBookChapter(book: Book): Flowable<Chapter> {
        return Observable.fromIterable(book.chapterList).map {
            it.url
        }.flatMap {
            sources[it.domain()]!!.getChapterContent(it).flatMap(localSource::saveChapter)
        }.toFlowable(BackpressureStrategy.LATEST)
    }



    fun deleteBook(bookName: String, author: String) = localSource.deleteBook(bookName, author)


    interface RemoteSource {
        fun getChapterContent(url: String): Observable<Chapter>
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
        fun saveBooks(books: List<Pair<BookSourceRecord, List<Book>>>): Single<List<Book>>
        fun deleteBook(bookName: String, author: String): Observable<String>
    }

}
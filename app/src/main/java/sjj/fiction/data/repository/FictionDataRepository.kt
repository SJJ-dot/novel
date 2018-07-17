package sjj.fiction.data.repository

import android.arch.paging.DataSource
import android.arch.paging.LivePagedListBuilder
import android.arch.paging.PagedList
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import sjj.alog.Log
import sjj.fiction.data.source.local.LocalFictionDataSource
import sjj.fiction.data.source.remote.aszw.AszwFictionDataSource
import sjj.fiction.data.source.remote.biquge.BiqugeDataSource
import sjj.fiction.data.source.remote.dhzw.DhzwDataSource
import sjj.fiction.data.source.remote.liumao.LiuMaoDataSource
import sjj.fiction.data.source.remote.yunlaige.YunlaigeDataSource
import sjj.fiction.model.Book
import sjj.fiction.model.BookSourceRecord
import sjj.fiction.model.Chapter
import sjj.fiction.util.concat
import sjj.fiction.util.domain
import sjj.fiction.util.lazyFromIterable
import java.util.concurrent.TimeUnit

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
        input(LiuMaoDataSource())
    }

    fun search(search: String): Single<List<Pair<BookSourceRecord, List<Book>>>> {
        return Observable.fromIterable(sources.values).flatMap {
            if (search.isBlank()) {
                throw IllegalArgumentException("搜索内容不能为空")
            }
            it.search(search).doOnError {
                Log.e("搜索出错:$it",it)
            }.onErrorResumeNext(Observable.empty())
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

    fun getBookInBookSource(name: String, author: String): Flowable<Book> {
        return localSource.getBookInBookSource(name, author).doOnNext {
            if (it.updateTime < System.currentTimeMillis() - 1000 * 60 * 10 || it.intro.isBlank()) {
                refreshBook(it.url).subscribe()
            }
        }
    }

    fun refreshBook(url: String): Observable<Book> {
        return Observable.just(url).flatMap {
            sources[it.domain()]?.getBook(it) ?: throw Exception("未知源 $it")
        }.flatMap(localSource::refreshBook)
    }

    fun getBooks(): Flowable<List<Book>> = localSource.getAllReadingBook()


    fun cachedBookChapter(bookUrl: String): Flowable<Pair<Int, Int>> {
        return localSource.getUnLoadChapters(bookUrl).map { cs -> cs.mapIndexed { index, chapter -> index to cs.size to chapter } }.lazyFromIterable {
            loadChapter(it.second).map { _ -> it.first }.delay(500, TimeUnit.MILLISECONDS)
        }.concat().toFlowable(BackpressureStrategy.LATEST)
    }

    fun loadChapter(chapter: Chapter): Observable<Chapter> {
        return Observable.just(chapter).flatMap {
            sources[it.url.domain()]!!.getChapterContent(it)
        }.flatMap(localSource::updateChapter)
    }

    fun getChapter(url: String): Observable<Chapter> {
        return localSource.getChapter(url).flatMap {
            if (it.isLoadSuccess) {
                Observable.just(it)
            } else {
                loadChapter(it)
            }
        }
    }

    fun deleteBook(bookName: String, author: String) = localSource.deleteBook(bookName, author)

    fun saveBookSourceRecord(books: List<Pair<BookSourceRecord, List<Book>>>) = localSource.saveBookSourceRecord(books)

    fun getBookSource(name: String, author: String): Observable<List<String>> {
        return localSource.getBookSource(name, author)
    }

    fun setBookSource(name: String, author: String, url: String): Observable<Int> {
        return localSource.updateBookSource(name, author, url)
    }

    fun getReadIndex(name: String, author: String): Flowable<Int> {
        return localSource.getReadIndex(name, author)
    }

    fun setReadIndex(name: String, author: String, index: Int): Observable<Int> {
        return localSource.setReadIndex(name, author, index)
    }

    fun getLatestChapter(bookUrl: String): Observable<Chapter> {
        return localSource.getLatestChapter(bookUrl)
    }

    fun getChapters(url: String) = LivePagedListBuilder(localSource.getChapters(url), PagedList.Config.Builder()
            .setPageSize(50)
//            .setEnablePlaceholders(true) 默认true
            .build()).build()


    interface RemoteSource {
        fun getChapterContent(chapter: Chapter): Observable<Chapter>
        fun getBook(url: String): Observable<Book>
        fun domain(): String
        fun search(search: String): Observable<List<Book>>
    }

    interface LocalSource {
        fun saveBookSourceRecord(books: List<Pair<BookSourceRecord, List<Book>>>): Single<List<Book>>
        fun getBookInBookSource(name: String, author: String): Flowable<Book>
        fun refreshBook(book: Book): Observable<Book>
        fun getChapter(url: String): Observable<Chapter>
        fun updateChapter(chapter: Chapter): Observable<Chapter>
        fun getAllReadingBook(): Flowable<List<Book>>
        fun deleteBook(bookName: String, author: String): Observable<Int>
        fun getBookSource(name: String, author: String): Observable<List<String>>
        fun updateBookSource(name: String, author: String, url: String): Observable<Int>
        fun getReadIndex(name: String, author: String): Flowable<Int>
        fun setReadIndex(name: String, author: String, index: Int): Observable<Int>
        fun getLatestChapter(bookUrl: String): Observable<Chapter>
        fun getChapters(bookUrl: String): DataSource.Factory<Int, Chapter>
        fun getChapterIntro(bookUrl: String): Flowable<List<Chapter>>
        fun getUnLoadChapters(bookUrl: String): Observable<List<Chapter>>
    }

}
package sjj.novel.data.repository

import android.arch.paging.DataSource
import android.arch.paging.LivePagedListBuilder
import android.arch.paging.PagedList
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import sjj.alog.Log
import sjj.novel.R
import sjj.novel.data.source.local.LocalFictionDataSource
import sjj.novel.data.source.remote.CommonBookEngine
import sjj.novel.model.Book
import sjj.novel.model.BookSourceRecord
import sjj.novel.model.Chapter
import sjj.novel.util.concat
import sjj.novel.util.host
import sjj.novel.util.lazyFromIterable
import sjj.novel.util.resStr
import java.util.concurrent.TimeUnit

val novelDataRepository by lazy { NovelDataRepository() }

/**
 * Created by SJJ on 2017/9/2.
 */
class NovelDataRepository {
//    private val sources = listOf<NovelDataRepository.RemoteSource>(
//            DhzwDataSource(),
//            YunlaigeDataSource(),
//            AszwFictionDataSource(),
//            BiqugeDataSource(),
//            LiuMaoDataSource(),
//            XBiquge6DataSource()
//            BaiDuDataSource()
//            CommonBookEngine()
//    )

//    private val sources = multable

    private val localSource: LocalSource = LocalFictionDataSource()

    private fun getSources(): Observable<List<CommonBookEngine>> {
        return novelSourceRepository.getAllBookParseRule().map {
            it.map {
                CommonBookEngine(it)
            }
        }.firstElement().toObservable()
    }

    private fun getSource(url: String):Observable<NovelDataRepository.RemoteSource> = getSources().map { list ->
        list.forEach {
            if (url.host.endsWith(it.topLevelDomain, true)) {
                return@map it
            }
        }
        throw IllegalArgumentException(R.string.unknown_source.resStr)
    }

    fun search(search: String): Observable<List<Pair<BookSourceRecord, List<Book>>>> {
        return getSources().flatMap { list ->
            if (search.isBlank()) {
                throw IllegalArgumentException(R.string.Search_content_cannot_be_empty.resStr)
            }
            Observable.fromIterable(list).filter {
                it.rule.enable
            }.switchIfEmpty(Observable.error(Exception(R.string.no_source_available.resStr))).flatMap { commonBookEngine ->
                commonBookEngine.search(search).doOnError {
                    Log.e(R.string.search_error.resStr(it.message), it)
                }.onErrorResumeNext(Observable.empty())
            }
        }.reduce(mutableMapOf<String, MutableList<Book>>()) { map, bs ->
            bs.forEach {
                map.getOrPut(it.name + it.author) { mutableListOf() }.add(it)
            }
            map
        }.toObservable().map {
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
        return localSource.getBookInBookSource(name, author)
//                .doOnNext {
//                    if (it.updateTime < System.currentTimeMillis() - 1000 * 60 * 10 || it.intro.isBlank()) {
//                        refreshBook(it.url).subscribe()
//                    }
//                }
    }

    fun refreshBook(url: String): Observable<Book> {
        return getSource(url).flatMap {
            it.getBook(url)
        }.flatMap(localSource::refreshBook)
    }

    fun getBooks(): Flowable<List<Book>> = localSource.getAllReadingBook()


    fun cachedBookChapter(bookUrl: String): Flowable<Pair<Int, Int>> {
        return localSource.getUnLoadChapters(bookUrl).map { cs -> cs.mapIndexed { index, chapter -> index to cs.size to chapter } }.lazyFromIterable {
            loadChapter(it.second).map { _ -> it.first }.delay(500, TimeUnit.MILLISECONDS)
        }.concat().toFlowable(BackpressureStrategy.LATEST)
    }

    fun loadChapter(chapter: Chapter): Observable<Chapter> {
        return getSource(chapter.url).flatMap {
            it.getChapterContent(chapter)
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
        /**
         *  top-level domain
         */
        val topLevelDomain: String

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
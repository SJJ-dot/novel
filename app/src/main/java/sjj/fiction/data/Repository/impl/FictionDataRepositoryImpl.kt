package sjj.fiction.data.Repository.impl

import com.raizlabs.android.dbflow.kotlinextensions.save
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import sjj.alog.Log
import sjj.fiction.data.Repository.FictionDataRepository
import sjj.fiction.data.source.local.LocalFictionDataSource
import sjj.fiction.data.source.remote.dhzw.DhzwDataSource
import sjj.fiction.data.source.remote.yunlaige.YunlaigeDataSource
import sjj.fiction.model.BookGroup
import sjj.fiction.model.Chapter
import sjj.fiction.util.def
import sjj.fiction.util.domain

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

    override fun search(search: String): Observable<List<BookGroup>> {

        return def(Schedulers.io()) {
            val map = mutableMapOf<String, BookGroup>()
            val count = object : Object() {
                var count = sources.size
                @Synchronized
                fun tryNotify() {
                    count--
                    if (count == 0) {
                        notify()
                    }
                }
            }
            var e: Throwable? = null
            sources.forEach {
                it.value.search(search).subscribe({
                    it.forEach {
                        map.getOrPut(it.name + it.author, { BookGroup(it) }).books.add(it)
                    }
                    count.tryNotify()
                }, {
                    e = it
                    Log.e("search error", it)
                    count.tryNotify()
                })
            }
            synchronized(count) { count.wait() }
            val list = map.values.toList()
            if (list.isEmpty() && e != null) {
                throw e!!
            }
            list
        }
    }

    override fun getSearchHistory(): Observable<List<String>> = localSource.getSearchHistory()
    override fun setSearchHistory(value: List<String>): Observable<List<String>> = localSource.setSearchHistory(value)
    override fun loadBookDetailsAndChapter(book: BookGroup, force: Boolean): Observable<BookGroup> = def {
        val lock = java.lang.Object()
        if (!force) {
            var localHas = false
            localSource.loadBookDetailsAndChapter(book.currentBook).subscribe({
                book.currentBook = it
                localHas = true
                synchronized(lock) { lock.notify() }
            }, {
                synchronized(lock) { lock.notify() }
            })
            synchronized(lock) { lock.wait() }
            if (localHas) {
                return@def book
            }
        }
        var e: Throwable? = null
        (sources[book.currentBook.url.domain()]
                ?.loadBookDetailsAndChapter(book.currentBook)
                ?.flatMap { localSource.saveBookGroup(listOf(book)) }
                ?.map { book }
                ?: error("未知源 ${book.currentBook.url}"))
                .subscribe({
                    synchronized(lock) { lock.notify() }
                }, {
                    e = it
                    synchronized(lock) { lock.notify() }
                })
        synchronized(lock) { lock.wait() }
        if (e != null) {
            throw e!!
        }
        return@def book
    }

    override fun loadBookChapter(chapter: Chapter): Observable<Chapter> = sources[chapter.url.domain()]
            ?.loadBookChapter(chapter)
            ?.flatMap {
                localSource.saveChapter(it)
            }
            ?: error("未知源 ${chapter.url}")

    override fun loadBookGroups(): Observable<List<BookGroup>> = localSource.loadBookGroups()

    override fun loadBookGroup(bookName: String, author: String): Observable<BookGroup> = localSource.loadBookGroup(bookName, author)
    override fun saveBookGroup(book: List<BookGroup>): Observable<List<BookGroup>> {
        return localSource.saveBookGroup(book)
    }
}
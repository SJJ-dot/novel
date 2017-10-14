package sjj.fiction.data.Repository.impl

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
import sjj.fiction.model.Url
import sjj.fiction.util.def

/**
 * Created by SJJ on 2017/9/3.
 */
class FictionDataRepositoryImpl : FictionDataRepository {
    private val sources = mutableMapOf<Url, FictionDataRepository.RemoteSource>()
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
            sources.forEach {
                it.value.search(search).subscribe({
                    it.forEach {
                        map.getOrPut(it.name + it.author, { BookGroup(it) }).books.add(it)
                    }
                    count.tryNotify()
                }, {
                    Log.e("search error", it)
                    count.tryNotify()
                })
            }
            synchronized(count) { count.wait() }
            map.values.toList()
        }.observeOn(
                Schedulers.io()
        ).doOnNext {
            val lock = java.lang.Object()
            localSource.getSearchHistory().flatMap {
                val set = it.toMutableSet()
                set.add(search)

                localSource.setSearchHistory(set.toList())
            }.subscribe({
                synchronized(lock) { lock.notify()}
            }, {
                synchronized(lock) { lock.notify()}
            })
            synchronized(lock) {
                lock.wait()
            }
        }.observeOn(AndroidSchedulers.mainThread())
    }

    override fun getSearchHistory(): Observable<List<String>> = localSource.getSearchHistory()

    override fun loadBookDetailsAndChapter(book: BookGroup): Observable<BookGroup> = sources[book.currentBook.url.domain()]?.loadBookDetailsAndChapter(book.currentBook)?.map { book } ?: error("未知源 ${book.currentBook.url}")

    override fun loadBookChapter(chapter: Chapter): Observable<Chapter> = sources[chapter.url.domain()]?.loadBookChapter(chapter) ?: error("未知源 ${chapter.url}")
}
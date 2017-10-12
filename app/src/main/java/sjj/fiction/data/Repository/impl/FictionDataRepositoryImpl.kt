package sjj.fiction.data.Repository.impl

import io.reactivex.Observable
import sjj.alog.Log
import sjj.fiction.data.Repository.FictionDataRepository
import sjj.fiction.data.source.remote.dhzw.DhzwDataSource
import sjj.fiction.data.source.remote.yunlaige.YunlaigeDataSource
import sjj.fiction.model.Book
import sjj.fiction.model.Chapter
import sjj.fiction.model.SearchResultBook
import sjj.fiction.model.Url

/**
 * Created by SJJ on 2017/9/3.
 */
class FictionDataRepositoryImpl : FictionDataRepository {
    private var sources = mutableMapOf<Url, FictionDataRepository.Source>()

    init {
        val dh: FictionDataRepository.Source = DhzwDataSource()
        sources[dh.domain()] = dh
        val yu: FictionDataRepository.Source = YunlaigeDataSource()
        sources[yu.domain()] = yu
    }

    override fun search(search: String): Observable<List<SearchResultBook>> {
        return Observable.combineLatest(sources.map { it.value.search(search) }) { t ->
            val list = mutableListOf<SearchResultBook>()
            t.toList().forEach {s->
                s as List<SearchResultBook>
                Log.e(s)
                s.forEach {
                    val find = list.find { r -> r.name == it.name && r.author == it.author }
                    if (find == null) {
                        list.add(it)
                    } else {
                        find.origins.removeAll(it.origins)
                        find.origins.addAll(it.origins)
                    }
                }
            }
            list
        }
    }

    override fun loadBookDetailsAndChapter(searchResultBook: SearchResultBook): Observable<Book> = sources[Url(searchResultBook.url.domain)]?.loadBookDetailsAndChapter(searchResultBook) ?: error("未知源 ${searchResultBook.url}")

    override fun loadBookChapter(chapter: Chapter): Observable<Chapter> = sources[Url(chapter.url.domain)]?.loadBookChapter(chapter) ?: error("未知源 ${chapter.url}")
}
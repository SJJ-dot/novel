package sjj.fiction.data.source.remote.dhzw

import io.reactivex.Observable
import org.jsoup.Jsoup
import sjj.alog.Log
import sjj.fiction.data.Repository.FictionDataRepository
import sjj.fiction.data.source.remote.HttpDataSource
import sjj.fiction.model.Book
import sjj.fiction.model.SearchResultBook
import sjj.fiction.model.Url
import java.net.URLEncoder

/**
 * Created by SJJ on 2017/9/3.
 */
class DhzwDataSource : HttpDataSource(), FictionDataRepository.Source {

    override fun baseUrl(): String = "http://www.dhzw.org"

    private val service = create(HttpInterface::class.java)

    override fun search(search: String): Observable<List<SearchResultBook>> {
        return service.search(URLEncoder.encode(search, "gbk"))
                .map {
                    Log.e(it)
                    val elementsByClass = Jsoup.parse(it).body().getElementsByClass("main-html")
                    val results = List(elementsByClass.size) {
                        val element = elementsByClass[it]
                        val ahref = element.select("a[href]")[0]
                        SearchResultBook(ahref.text(), Url(ahref.attr("href")))
                    }
                    results
                }
    }

    override fun loadBookCoverAndOrigin(searchResultBook: SearchResultBook): Observable<Book> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
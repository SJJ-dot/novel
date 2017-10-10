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
                    val elementsByClass = Jsoup.parse(it).body().getElementById("newscontent").getElementsByTag("ul")[0].getElementsByTag("li")
                    val results = List(elementsByClass.size) {
                        val ahref = elementsByClass[it].child(1).child(0)
                        SearchResultBook(ahref.text(), Url(ahref.attr("href")), elementsByClass[it].child(3).child(0).text())
                    }
                    results
                }
    }

    override fun loadBookDetailsAndChapter(searchResultBook: SearchResultBook): Observable<Book> {
        return service.loadBookDetailsAndChapter(searchResultBook.url.url).map {
            Log.e(it)
            Book(searchResultBook.name, listOf())
        }
    }
}
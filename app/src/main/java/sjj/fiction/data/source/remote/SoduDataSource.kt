package sjj.fiction.data.source.remote

import io.reactivex.Observable
import org.jsoup.Jsoup
import sjj.alog.Log
import sjj.fiction.data.Repository.SoduDataRepository
import sjj.fiction.data.service.SoduService
import sjj.fiction.model.SearchResultBook
import sjj.fiction.model.Url
import java.net.URLEncoder
import java.nio.charset.Charset

/**
 * Created by SJJ on 2017/9/3.
 */
class SoduDataSource : HttpDataSource(), SoduDataRepository.Source {
    override fun baseUrl(): String = "http://www.sodu.cc"

    private val service = create(SoduService::class.java)

    override fun search(search: String): Observable<List<SearchResultBook>> {
        return service.search("哈利波特")
                .map {
                    val elementsByClass = Jsoup.parse(it).body().getElementsByClass("main-html")
                    val results = List(elementsByClass.size) {
                        val element = elementsByClass[it]
                        val ahref = element.select("a[href]")[0]
                        SearchResultBook(ahref.text(), Url(ahref.attr("href")))
                    }
                    results
                }
    }

}
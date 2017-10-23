package sjj.fiction.data.source.remote.aszw

import io.reactivex.Observable
import org.jsoup.Jsoup
import sjj.alog.Log
import sjj.fiction.data.Repository.FictionDataRepository
import sjj.fiction.data.source.remote.HttpDataSource
import sjj.fiction.data.source.remote.HttpInterface
import sjj.fiction.model.Book
import sjj.fiction.model.Chapter
import sjj.fiction.util.domain
import sjj.fiction.util.errorObservable
import java.net.URLEncoder

/**
 * Created by Administrator on 2017/10/23.
 */
class AszwFictionDataSource : HttpDataSource(), FictionDataRepository.RemoteSource {
    private val service = create<HttpInterface>()
    override fun baseUrl(): String = "http://www.aszw.org"

    override fun domain() = baseUrl().domain()

    override fun search(search: String): Observable<List<Book>> {
        return service.searchForGBK("modules/article/search.php", mapOf("searchkey" to URLEncoder.encode(search, "gbk"))).map {
            val element = Jsoup.parse(it).body().getElementById("content").getElementsByTag("tbody")[0].getElementsByTag("tr")
            val list = mutableListOf<Book>()
            for (i in 1 until element.size) {
                val select = element[i].select("a[href]")
                val url = select[0].attr("href")
                val name = select[0].text()
                val author = select[2].text()
                list.add(Book(url, name, author))
            }
            list
        }
    }

    override fun loadBookChapter(chapter: Chapter): Observable<Chapter> {
        return errorObservable("not implemented")
    }

    override fun loadBookDetailsAndChapter(book: Book): Observable<Book> {
        return errorObservable("not implemented")
    }
}
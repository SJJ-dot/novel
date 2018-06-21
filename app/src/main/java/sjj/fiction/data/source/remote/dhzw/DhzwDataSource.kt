package sjj.fiction.data.source.remote.dhzw

import io.reactivex.Observable
import org.jsoup.Jsoup
import sjj.fiction.data.repository.FictionDataRepository
import sjj.fiction.data.source.remote.HttpDataSource
import sjj.fiction.data.source.remote.HttpInterface
import sjj.fiction.model.*
import sjj.fiction.util.domain
import java.net.URLEncoder

/**
 * Created by SJJ on 2017/9/3.
 */
class DhzwDataSource : HttpDataSource(), FictionDataRepository.RemoteSource {
    override val baseUrl: String = "https://www.dhzw.org"
    override fun domain(): String = baseUrl.domain()

    private val service = create<HttpInterface>()

    override fun search(search: String): Observable<List<Book>> {
        return service.searchForGBK("modules/article/search.php", mapOf(Pair("searchkey", URLEncoder.encode(search, "gbk"))))
                .map {
                    val elementsByClass = Jsoup.parse(it).body().getElementById("newscontent").getElementsByTag("ul")[0].getElementsByTag("li")
                    val results = List(elementsByClass.size) {
                        val ahref = elementsByClass[it].child(1).select("a[href]")[0]
                        Book(ahref.attr("href"), ahref.text(), elementsByClass[it].child(3).child(0).text())
                    }
                    results
                }
    }

    override fun getChapterContent(url: String): Observable<Chapter> {
        return service.loadHtmlForGBK(url).map {
            val parse = Jsoup.parse(it).getElementById("BookText")
            val chapter = Chapter()
            chapter.url = url
            chapter.content = parse.html()
            chapter.isLoadSuccess = true
            chapter
        }
    }

    override fun getBook(url: String): Observable<Book> {
        return service.loadHtmlForGBK(url).map {
            val parse = Jsoup.parse(it, url).body()
            val book = Book()
            book.url = url
            val infotitle = parse.getElementsByClass("infotitle")[0]
            book.name = infotitle.child(0).text()
            book.author = infotitle.child(1).text().split("：").last()
            book.bookCoverImgUrl = parse.getElementById("fmimg").select("[src]")[0].attr("src")
            book.intro = parse.getElementById("info").child(1).text()
            book.chapterList = parse.getElementById("list").select("a[href]").mapIndexed { index, e -> Chapter(e.attr("abs:href"), book.url, index = index, chapterName = e.text()) }
            book.chapterListUrl = book.url
            book
        }
    }
}

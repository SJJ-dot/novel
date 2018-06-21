package sjj.fiction.data.source.remote.aszw

import io.reactivex.Observable
import org.jsoup.Jsoup
import sjj.fiction.data.repository.FictionDataRepository
import sjj.fiction.data.source.remote.HttpDataSource
import sjj.fiction.data.source.remote.HttpInterface
import sjj.fiction.model.Book
import sjj.fiction.model.Chapter
import sjj.fiction.util.domain
import java.net.URLEncoder

/**
 * Created by Administrator on 2017/10/23.
 */
class AszwFictionDataSource : HttpDataSource(), FictionDataRepository.RemoteSource {
    override val baseUrl: String =  "https://www.aszw.org"
    private val service = create<HttpInterface>()

    override fun domain() = baseUrl.domain()

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

    override fun getChapterContent(url: String): Observable<Chapter> {
        return service.loadHtmlForGBK(url).map {
            val document = Jsoup.parse(it)
            val parse = document.getElementById("contents")
            val chapter = Chapter()
            chapter.url = url
            chapter.chapterName =document.getElementsByClass("bdb")[0].text()
            chapter.content = parse.html()
            chapter.isLoadSuccess = true
            chapter
        }
    }

    override fun getBook(url: String): Observable<Book> {
        return service.loadHtmlForGBK(url).map {
            val body = Jsoup.parse(it, url).body()
            val parse = body.getElementsByClass("info")[0]
            val book  = Book()
            book.url = url
            val btitle = parse.getElementsByClass("btitle")
            book.name = btitle[0].child(0).text()
            book.author = btitle[0].child(1).text().trim().split("：").last()
            book.bookCoverImgUrl = parse.select("[src]")[0].attr("src")
            book.intro = parse.getElementsByClass("book")[0].getElementsByClass("js")[0].text()
            book.chapterList = body.getElementById("at").select("a[href]").mapIndexed { index, e -> Chapter(e.attr("abs:href"), book.url, index = index, chapterName = e.text()) }
            book.chapterListUrl = book.url
            book
        }
    }
}
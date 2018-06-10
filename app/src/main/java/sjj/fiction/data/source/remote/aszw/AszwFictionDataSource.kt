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

    override fun loadBookChapter(chapter: Chapter): Observable<Chapter> {
        return service.loadHtmlForGBK(chapter.url).map {
            val parse = Jsoup.parse(it).getElementById("contents")
            chapter.content = parse.html()
            chapter.isLoadSuccess = true
            chapter
        }
    }

    override fun loadBookDetailsAndChapter(book: Book): Observable<Book> {
        return service.loadHtmlForGBK(book.url).map {
            val body = Jsoup.parse(it, book.url).body()
            val parse = body.getElementsByClass("info")[0]
            book.bookCoverImgUrl = parse.select("[src]")[0].attr("src")
            book.intro = parse.getElementsByClass("book")[0].getElementsByClass("js")[0].text()
            book.chapterList = body.getElementById("at").select("a[href]").mapIndexed { index, e -> Chapter(e.attr("abs:href"), book.id, index = index, chapterName = e.text()) }
            book.chapterListUrl = book.url
            book
        }
    }
}
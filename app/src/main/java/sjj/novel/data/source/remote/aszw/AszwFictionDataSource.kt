package sjj.novel.data.source.remote.aszw

import io.reactivex.Observable
import org.jsoup.Jsoup
import sjj.alog.Log
import sjj.novel.data.repository.NovelDataRepository
import sjj.novel.data.source.remote.HttpDataSource
import sjj.novel.data.source.remote.HttpInterface
import sjj.novel.model.Book
import sjj.novel.model.Chapter
import java.net.URLEncoder

/**
 * Created by Administrator on 2017/10/23.
 */
class AszwFictionDataSource : HttpDataSource(), NovelDataRepository.RemoteSource {
    override val baseUrl: String = "https://www.aszw.org/"
    private val service = create<HttpInterface>()
    override val topLevelDomain: String = "aszw.org"

    override fun search(search: String): Observable<List<Book>> {
        return service.searchPost("modules/article/search.php", mapOf("searchkey" to URLEncoder.encode(search, "gbk"))).map {
            val parse = Jsoup.parse(it.body())
            val select1 = parse.select("#content>tbody:first-child>.tr")
            Log.e(select1)
            val element = parse.body().getElementById("content").getElementsByTag("tbody")[0].getElementsByTag("tr")
            Log.e(element)
            val list = mutableListOf<Book>()
            for (i in 1 until element.size) {
                val select = element[i].select("a[href]")
                val url = select[0].absUrl("href")
                val name = select[0].text()
                val author = select[2].text()
                list.add(Book(url, name, author))
            }
            list
        }
    }

    override fun getChapterContent(chapter: Chapter): Observable<Chapter> {
        return service.loadHtml(chapter.url).map {
            val document = Jsoup.parse(it.body())
            val parse = document.getElementById("contents")
            chapter.chapterName = document.getElementsByClass("bdb")[0].text()
            chapter.content = parse.html()
            chapter.isLoadSuccess = true
            chapter
        }
    }

    override fun getBook(url: String): Observable<Book> {
        return service.loadHtml(url).map {
            val body = Jsoup.parse(it.body(), url).body()
            val parse = body.getElementsByClass("info")[0]
            val book = Book()
            book.url = url
            val btitle = parse.getElementsByClass("btitle")
            book.name = btitle[0].child(0).text()
            book.author = btitle[0].child(1).text().trim().split("：").last()
            book.bookCoverImgUrl = parse.select("[src]")[0].absUrl("src")
            book.intro = parse.getElementsByClass("book")[0].getElementsByClass("js")[0].text()
            book.chapterList = body.getElementById("at").select("a[href]").mapIndexed { index, e -> Chapter(e.absUrl("abs:href"), book.url, index = index, chapterName = e.text()) }
            book.chapterListUrl = book.url
            book
        }
    }
}
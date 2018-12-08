package sjj.novel.data.source.remote.dhzw

import io.reactivex.Observable
import org.jsoup.Jsoup
import sjj.novel.data.repository.NovelDataRepository
import sjj.novel.data.source.remote.HttpDataSource
import sjj.novel.data.source.remote.HttpInterface
import sjj.novel.model.Book
import sjj.novel.model.Chapter
import java.net.URLEncoder

/**
 * Created by SJJ on 2017/9/3.
 */
class DhzwDataSource : HttpDataSource(), NovelDataRepository.RemoteSource {
    override val baseUrl: String = "https://www.dhzw.org/"
    override val topLevelDomain: String = "dhzw.org"
    private val service = create<HttpInterface>()

    override fun search(search: String): Observable<List<Book>> {
        return service.searchPost("modules/article/search.php", mapOf(Pair("searchkey", URLEncoder.encode(search, "gbk"))))
                .map {
                    val elementsByClass = Jsoup.parse(it.body()).body().getElementById("newscontent").getElementsByTag("ul")[0].getElementsByTag("li")
                    val results = List(elementsByClass.size) {
                        val ahref = elementsByClass[it].child(1).select("a[href]")[0]
                        Book(ahref.absUrl("href"), ahref.text(), elementsByClass[it].child(3).child(0).text())
                    }
                    results
                }
    }

    override fun getChapterContent(chapter: Chapter): Observable<Chapter> {
        return service.loadHtml(chapter.url).map {
            val parse = Jsoup.parse(it.body()).getElementById("BookText")
            chapter.content = parse.html()
            chapter.isLoadSuccess = true
            chapter
        }
    }

    override fun getBook(url: String): Observable<Book> {
        return service.loadHtml(url).map {
            val parse = Jsoup.parse(it.body(), it.baseUrl).body()
            val book = Book()
            book.url = url
            val infotitle = parse.getElementsByClass("infotitle")[0]
            book.name = infotitle.child(0).text()
            book.author = infotitle.child(1).text().split("：").last()
            book.bookCoverImgUrl = parse.getElementById("fmimg").select("[src]")[0].absUrl("src")
            book.intro = parse.getElementById("info").child(1).text()
            book.chapterList = parse.getElementById("list").select("a[href]").mapIndexed { index, e -> Chapter(e.absUrl("abs:href"), book.url, index = index, chapterName = e.text()) }
            book.chapterListUrl = book.url
            book
        }
    }
}
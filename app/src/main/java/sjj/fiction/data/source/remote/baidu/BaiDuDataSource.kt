package sjj.fiction.data.source.remote.baidu

import io.reactivex.Observable
import org.jsoup.Jsoup
import sjj.fiction.data.repository.FictionDataRepository
import sjj.fiction.data.source.remote.HttpDataSource
import sjj.fiction.data.source.remote.HttpInterface
import sjj.fiction.model.Book
import sjj.fiction.model.Chapter

class BaiDuDataSource() : HttpDataSource(), FictionDataRepository.RemoteSource {
    override val baseUrl: String = "https://m.baidu.com/"
    private val service = create<HttpInterface>()
    override fun getChapterContent(chapter: Chapter): Observable<Chapter> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getBook(url: String): Observable<Book> {
        return service.loadHtml(url).map {
            val parse = Jsoup.parse(it.body(), url).body()
            val book = Book()
            book.url = url
            val info = parse.getElementById("info")
            book.name = info.child(0).text()
            book.author = info.child(1).text().trim().split("：").last()
            book.bookCoverImgUrl = parse.getElementById("fmimg").select("[src]")[0].absUrl("src")
            book.intro = parse.getElementById("intro").child(0).text()
            val children = parse.getElementById("list").child(0).children()
            val last = children.indexOfLast { it.tag().name == "dt" }
            book.chapterListUrl = url
            book.chapterList = children.subList(last+1,children.size)
                    .map { it.select("a[href]")[0] }
                    .mapIndexed { index, e -> Chapter(e.absUrl("abs:href"),book.url,index = index, chapterName = e.text()) }
            book
        }
    }

    override val topLevelDomain: String = "baidu.com"

    override fun search(search: String): Observable<List<Book>> {
        return service.searchGet("s", mapOf("word" to search)).map {
            val content = Jsoup.parse(it.body(), it.baseUrl).getElementsByAttributeValue("srcid", "nvl_trans")[0].getElementsByClass("c-result-content")[0]
            val url = content.getElementsByClass("wa-nvl-trans-btn-wrap c-span3").select("a[href]")[0].absUrl("href")
            val name = content.getElementsByTag("header")[0].getElementsByTag("em")[0].text()
            val author = content.getElementsByTag("section")[0].getElementsByClass("c-color wa-nvl-trans-author")[0]
                    .text().split(": ")[1]
            listOf(Book(url,name, author))
        }
    }

}
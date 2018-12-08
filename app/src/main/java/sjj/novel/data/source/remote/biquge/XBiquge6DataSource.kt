package sjj.novel.data.source.remote.biquge

import io.reactivex.Observable
import org.jsoup.Jsoup
import sjj.novel.data.repository.NovelDataRepository
import sjj.novel.data.source.remote.HttpDataSource
import sjj.novel.data.source.remote.HttpInterface
import sjj.novel.model.Book
import sjj.novel.model.Chapter

/**
 * Created by SJJ on 2017/11/3.
 */
class XBiquge6DataSource() : HttpDataSource(), NovelDataRepository.RemoteSource {
    override val baseUrl: String = "https://www.xbiquge6.com/"
    private val service = create<HttpInterface>()
    override val topLevelDomain: String = "xbiquge6.com"

    override fun search(search: String): Observable<List<Book>> {
        return service.searchGet("search.php", mapOf("keyword" to search)).map { it ->
            Jsoup.parse(it.body()).body().getElementsByClass("result-list").map {
                val detail = it.getElementsByClass("result-game-item-detail")[0]
                val title = detail.getElementsByClass("result-item-title result-game-item-title")[0].getElementsByClass("result-game-item-title-link")[0]
                val bookUrl = title.absUrl("href")
                val bookName = title.getElementsByAttribute("title").text().trim()
                val author = detail.getElementsByClass("result-game-item-info-tag")[0].child(1).text().trim()
                Book(bookUrl,bookName,author)
            }.toList()
        }
    }

    override fun getChapterContent(chapter: Chapter): Observable<Chapter> {
        return service.loadHtml(chapter.url).map {
            val get = Jsoup.parse(it.body())
            chapter.chapterName = get.getElementsByClass("box_con")[0].getElementsByClass("bookname")[0].child(0).text()
            val parse = get.getElementById("content")
            chapter.content = parse.html()
            chapter.isLoadSuccess = true
            chapter
        }
    }

    override fun getBook(url: String): Observable<Book> {
        return service.loadHtml(url).map { it ->
            val parse = Jsoup.parse(it.body(), it.baseUrl).body()
            val book = Book()
            book.url = url
            val info = parse.getElementById("maininfo")
            book.name = info.child(0).child(0).text()
            book.author = info.child(0).child(1).text().trim().split("：").last()
            book.bookCoverImgUrl = parse.getElementById("sidebar").getElementById("fmimg").select("[src]")[0].absUrl("src")
            book.intro = parse.getElementById("maininfo").getElementById("intro").child(0).text()
            val children = parse.getElementById("list").child(0).children()
            book.chapterListUrl = url
            book.chapterList = children.subList(1, children.size)
                    .map { it.select("a[href]")[0] }
                    .mapIndexed { index, e -> Chapter(e.absUrl("abs:href"), book.url, index = index, chapterName = e.text()) }
            book
        }
    }


}
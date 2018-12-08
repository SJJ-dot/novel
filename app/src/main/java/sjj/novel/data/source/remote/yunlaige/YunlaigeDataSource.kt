package sjj.novel.data.source.remote.yunlaige

import io.reactivex.Observable
import org.jsoup.Jsoup
import sjj.novel.data.repository.NovelDataRepository
import sjj.novel.data.source.remote.HttpDataSource
import sjj.novel.data.source.remote.HttpInterface
import sjj.novel.model.Book
import sjj.novel.model.Chapter
import java.net.URLEncoder

/**
 * Created by SJJ on 2017/10/11.
 */
class YunlaigeDataSource : HttpDataSource(), NovelDataRepository.RemoteSource {
    override val baseUrl: String = "http://www.yunlaige.com/"
    private val service = create<HttpInterface>()
    override val topLevelDomain: String = "yunlaige.com"

    override fun search(search: String): Observable<List<Book>> {
        val url = "http://www.yunlaige.com/modules/article/search.php"
        return service.searchPost(url, mapOf("searchkey" to URLEncoder.encode(search, "gbk"))).map {
            val document = Jsoup.parse(it.body())
            try {
                document.body().getElementsByClass("chart-dashed-list")[0].children().map {
                    val child1 = it.child(1).child(0).child(0).select("a[href]")[0]
                    Book(child1.absUrl("href"), child1.text(), it.child(1).child(1).text().split("/")[0])
                }
            } catch (e: Exception) {
                val element = document.body().getElementsByClass("book-info")[0]
                val info = element.getElementsByClass("info")[0]
                val name = info.child(0).child(0).text()
                val author = info.child(1).child(0).text()
                val split = document.metaProp("og:novel:read_url").split("/")
                listOf(Book(it.baseUrl, name, author))
            }
        }
    }

    override fun getBook(url: String): Observable<Book> {
        return service.loadHtml(url).map {
            //            val name: String, val author: String, val coverImgUrl: Url, val intro: String, val latestChapter: Chapter, val chapterList: List<Chapter>
            val element = Jsoup.parse(it.body()).body().getElementsByClass("book-info")[0]
            val info = element.getElementsByClass("info")[0]
            val book = Book()
            book.url = url
            book.name = info.child(0).child(0).text()
            book.author = info.child(1).child(0).text()
            book.bookCoverImgUrl = element.select("a[href]")[0].absUrl("href")
            book.intro = info.child(2).text()
            book.chapterListUrl = info.child(3).select("a[href]")[0].absUrl("href")
            book
        }.flatMap {
            loadChapterList(it)
        }
    }
    private fun loadChapterList(book: Book): Observable<Book> {
        return service.loadHtml(book.chapterListUrl).map {
            book.chapterList = Jsoup.parse(it.body(), it.baseUrl).getElementById("contenttable").child(0).select("a[href]").mapIndexed { index, e ->
                Chapter(e.absUrl("abs:href"), book.url, index, e.text())
            }
            book
        }
    }

    override fun getChapterContent(chapter: Chapter): Observable<Chapter> {
        return service.loadHtml(chapter.url).map {
            val element = Jsoup.parse(it.body()).getElementById("content")
            chapter.content = element.html()
            chapter.isLoadSuccess = true
            chapter
        }
    }

}
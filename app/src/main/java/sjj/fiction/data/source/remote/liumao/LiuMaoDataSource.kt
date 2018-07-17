package sjj.fiction.data.source.remote.liumao

import io.reactivex.Observable
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import sjj.alog.Log
import sjj.fiction.data.repository.FictionDataRepository
import sjj.fiction.data.source.remote.HttpDataSource
import sjj.fiction.data.source.remote.HttpInterface
import sjj.fiction.model.Book
import sjj.fiction.model.Chapter
import sjj.fiction.util.domain
import java.net.URLEncoder

class LiuMaoDataSource : HttpDataSource(), FictionDataRepository.RemoteSource {
    override val baseUrl: String = "http://www.6mao.com/"
    override fun domain(): String = baseUrl.domain()

    private val service = create<HttpInterface>()

    override fun search(search: String): Observable<List<Book>> {
        return service.searchForGBK("/modules/article/ss.php", mapOf(Pair("searchkey", URLEncoder.encode(search, "gbk")))).map {
            val document = Jsoup.parse(it)
            try {
                val element = document.body().getElementsByClass("_content")[0].getElementsByClass("grid")[0].child(0).children()
                val list = mutableListOf<Book>()
                for (i in 1 until element.size) {
                    list.add(element[i].run {
                        val href = child(0).child(0)
                        Book(href.absUrl("href"), href.text(), child(2).text())
                    })
                }
                list
            } catch (e: Exception) {
                val url = document.metaProp("og:novel:read_url")
                val name = document.metaProp("og:novel:book_name")
                val author = document.metaProp("og:novel:author")
                listOf(Book(url, name, author))
            }
        }
    }

    override fun getChapterContent(chapter: Chapter): Observable<Chapter> {
        return service.loadHtmlForGBK(chapter.url).map {
            val element = Jsoup.parse(it).getElementById("neirong")
            chapter.content = element.html()
            chapter.isLoadSuccess = true
            chapter
        }
    }

    override fun getBook(url: String): Observable<Book> {
        return service.loadHtmlForGBK(url).map {
            val book = Book()
            val document = Jsoup.parse(it, url)
            val intro = document.getElementsByClass("intro")[0]
            book.url = url
            book.name = intro.getElementsByClass("title")[0].child(0).text()
            book.author = document.metaProp("og:novel:author")
            book.bookCoverImgUrl = intro.child(0).child(0).absUrl("src")
            book.intro = document.metaProp("og:description")
            book.chapterList = document.getElementsByClass("liebiao_bottom")[0].child(0).children().map { it.select("a[href]")[0] }.mapIndexed { index, e ->
                Chapter(e.absUrl("abs:href"), book.url, index = index, chapterName = e.text())
            }
            book.chapterListUrl = book.url
            book
        }
    }




}
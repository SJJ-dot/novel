package sjj.fiction.data.source.remote.yunlaige

import io.reactivex.Observable
import org.jsoup.Jsoup
import sjj.alog.Log
import sjj.fiction.App
import sjj.fiction.data.Repository.FictionDataRepository
import sjj.fiction.data.source.remote.HttpDataSource
import sjj.fiction.data.source.remote.HttpInterface
import sjj.fiction.model.*
import java.net.URLEncoder

/**
 * Created by SJJ on 2017/10/11.
 */
class YunlaigeDataSource : HttpDataSource(), FictionDataRepository.Source {
    private val service = create<HttpInterface>()

    override fun domain(): Url = Url(baseUrl())
    override fun search(search: String): Observable<List<SearchResultBook>> {
        val url = "http://www.yunlaige.com/modules/article/search.php"
        val encode = URLEncoder.encode(search, "gbk");
        return service.searchForGBK(url, mapOf("searchkey" to encode)).map {
            try {
                Jsoup.parse(it).body().getElementsByClass("chart-dashed-list")[0].children().map {
                    val child1 = it.child(1).child(0).child(0).select("a[href]")[0]
                    SearchResultBook(child1.text(), it.child(1).child(1).text().split("/")[0], Url(child1.attr("href")))
                }
            } catch (e: Exception) {
                val element = Jsoup.parse(it).body().getElementsByClass("book-info")[0]
                val info = element.getElementsByClass("info")[0]
                val name = info.child(0).child(0).text()
                val author = info.child(1).child(0).text()
                listOf(SearchResultBook(name, author, Url(App.app.config.getHttp302Url(url, "searchkey=$encode"))))
            }
        }
    }

    override fun loadBookDetailsAndChapter(searchResultBook: SearchResultBook): Observable<Book> {
        return service.loadHtmlForGBK(searchResultBook.url.url).map {
            //            val name: String, val author: String, val coverImgUrl: Url, val intro: String, val latestChapter: Chapter, val chapterList: List<Chapter>
            val element = Jsoup.parse(it).body().getElementsByClass("book-info")[0]
            val info = element.getElementsByClass("info")[0]
            val name = info.child(0).child(0).text()
            val author = info.child(1).child(0).text()
            val coverImgUrl = element.select("a[href]")[0].attr("href")
            val intro = info.child(2).text()
            val child = element.getElementsByClass("tabnewlist")[0].child(0).child(0).child(0).child(0).select("a[href]")[0]
            val latestChapterval = Chapter(child.text(), Url(child.attr("href")))
            val bookContent = BookContent(searchResultBook.url,Url(coverImgUrl),latestChapterval,Url(info.child(3).select("a[href]")[0].attr("href")))
            val book = Book(name, author, intro,bookContent)
            book
        }.flatMap {
            loadChapterList(it)
        }
    }

    private fun loadChapterList(book: Book): Observable<Book> {
        return service.loadHtmlForGBK(book.content.chapterListOrigin.url).map {
            book.content.chapterList = Jsoup.parse(it, book.content.chapterListOrigin.url).getElementById("contenttable").child(0).select("a[href]").map {
                Log.e(it)
                Chapter(it.text(), Url(it.attr("abs:href")))
            }
            book
        }
    }


    override fun loadBookChapter(chapter: Chapter): Observable<Chapter> {
        return service.loadHtmlForGBK(chapter.url.url).map {
            val element = Jsoup.parse(it).getElementById("content")
            chapter.content = element.html()
            chapter
        }
    }

    override fun baseUrl(): String = "http://www.yunlaige.com"
}
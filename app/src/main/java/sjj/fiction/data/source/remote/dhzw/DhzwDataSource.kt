package sjj.fiction.data.source.remote.dhzw

import io.reactivex.Observable
import org.jsoup.Jsoup
import sjj.fiction.data.Repository.FictionDataRepository
import sjj.fiction.data.source.remote.HttpDataSource
import sjj.fiction.data.source.remote.HttpInterface
import sjj.fiction.model.Book
import sjj.fiction.model.Chapter
import sjj.fiction.model.SearchResultBook
import sjj.fiction.model.Url
import java.net.URLEncoder

/**
 * Created by SJJ on 2017/9/3.
 */
class DhzwDataSource : HttpDataSource(), FictionDataRepository.Source {
    override fun baseUrl(): String = "http://www.dhzw.org"

    private val service = create<HttpInterface>()

    override fun search(search: String): Observable<List<SearchResultBook>> {
        return service.searchForGBK("modules/article/search.php", mapOf(Pair("searchkey", URLEncoder.encode(search, "gbk"))))
                .map {
                    val elementsByClass = Jsoup.parse(it).body().getElementById("newscontent").getElementsByTag("ul")[0].getElementsByTag("li")
                    val results = List(elementsByClass.size) {
                        val ahref = elementsByClass[it].child(1).select("a[href]")[0]
                        SearchResultBook(ahref.text(), Url(ahref.attr("href")), elementsByClass[it].child(3).child(0).text())
                    }
                    results
                }
    }

    override fun loadBookDetailsAndChapter(searchResultBook: SearchResultBook): Observable<Book> {
        return service.loadHtmlForGBK(searchResultBook.url.url).map {
            val parse = Jsoup.parse(it, searchResultBook.url.url).body()
            val fmsrc = parse.getElementById("fmimg").select("a[href]")[0].attr("src")
            val info = parse.getElementById("info")
            val infoTitle = info.child(0)
            val name = infoTitle.child(0).text()
            val author = infoTitle.child(1).text().split("：")[1]
            val intro = info.child(1).text()
            val latest = info.child(2).child(0).select("a[href]")[0]
            val latestUrl = latest.attr("abs:href")
            val latestName = latest.text()
            val select = parse.getElementById("list").select("a[href]")
            val list = select.map { Chapter(it.text(), Url(it.attr("abs:href"))) }
            val book = Book(name, author, intro, searchResultBook.url, Url(fmsrc), Chapter(latestName, Url(latestUrl)), list)
            book.originUrls.add(searchResultBook.url)
            book
        }
    }

    override fun loadBookChapter(chapter: Chapter): Observable<Chapter> {
        return service.loadHtmlForGBK(chapter.url.url).map {
            val parse = Jsoup.parse(it).getElementById("BookText")
            chapter.content = parse.html()
            chapter
        }
    }
}

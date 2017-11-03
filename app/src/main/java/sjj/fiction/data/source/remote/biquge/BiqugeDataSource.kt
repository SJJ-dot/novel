package sjj.fiction.data.source.remote.biquge

import io.reactivex.Observable
import org.jsoup.Jsoup
import sjj.alog.Log
import sjj.fiction.data.Repository.FictionDataRepository
import sjj.fiction.data.source.remote.HttpDataSource
import sjj.fiction.data.source.remote.HttpInterface
import sjj.fiction.model.Book
import sjj.fiction.model.Chapter
import sjj.fiction.util.domain

/**
 * Created by SJJ on 2017/11/3.
 */
class BiqugeDataSource():HttpDataSource(),FictionDataRepository.RemoteSource {
    private val service = create<HttpInterface>()
    override fun baseUrl() = "http://www.biquge5200.com"
    override fun domain() = baseUrl().domain()
    override fun search(search: String): Observable<List<Book>> {
        return service.searchForGBKGET("/modules/article/search.php", mapOf("searchkey" to search)).map {
            val children = Jsoup.parse(it).body().getElementsByTag("tbody")[0].children()
            children.takeLast(children.size - 1).map {
                val element = it.select("a[href]")[0]
                Book(element.attr("href"),element.text(),it.child(2).text())
            }.toList()
        }
    }

    override fun loadBookChapter(chapter: Chapter): Observable<Chapter> {
        return service.loadHtmlForGBK(chapter.url).map {
            val parse = Jsoup.parse(it).getElementById("content")
            chapter.content = parse.html()
            chapter.isLoadSuccess = true
            chapter
        }
    }

    override fun loadBookDetailsAndChapter(book: Book): Observable<Book> {
        return service.loadHtmlForGBK(book.url).map {
            val parse = Jsoup.parse(it, book.url).body()
            Log.e( parse.getElementById("fmimg"))
            book.bookCoverImgUrl = parse.getElementById("fmimg").select("[src]")[0].attr("src")
            book.intro = parse.getElementById("intro").child(0).text()
            val children = parse.getElementById("list").child(0).children()
            val last = children.indexOfLast { it.tag().name == "dt" }
            book.chapterList = children.subList(last+1,children.size)
                    .map { it.select("a[href]") }
                    .mapIndexed { index, e -> Chapter(e.attr("abs:href"), book.id, index = index, chapterName = e.text()) }
            book.chapterListUrl = book.url
            book
        }
    }
}
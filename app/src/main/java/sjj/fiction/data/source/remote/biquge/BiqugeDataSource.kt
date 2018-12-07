package sjj.fiction.data.source.remote.biquge

import io.reactivex.Observable
import org.jsoup.Jsoup
import sjj.fiction.data.repository.FictionDataRepository
import sjj.fiction.data.source.remote.HttpDataSource
import sjj.fiction.data.source.remote.HttpInterface
import sjj.fiction.model.Book
import sjj.fiction.model.Chapter

/**
 * Created by SJJ on 2017/11/3.
 */
class BiqugeDataSource() : HttpDataSource(), FictionDataRepository.RemoteSource {
    override val baseUrl: String = "https://www.biquge5200.cc/"
    private val service = create<HttpInterface>()
    override val topLevelDomain: String = "biquge5200.cc"

    override fun search(search: String): Observable<List<Book>> {
        return service.searchGet("modules/article/search.php", mapOf("searchkey" to search)).map {
            val children = Jsoup.parse(it.body()).body().getElementsByTag("tbody")[0].children()
            children.takeLast(children.size - 1).map {
                val element = it.select("a[href]")[0]
                Book(element.absUrl("href"), element.text(), it.child(2).text())
            }.toList()
        }
    }

    override fun getChapterContent(chapter: Chapter): Observable<Chapter> {
        return service.loadHtml(chapter.url).map {
            val get = Jsoup.parse(it.body())
            val parse = get.getElementById("content")
            chapter.chapterName =get.getElementsByClass("bookname")[0].child(0).text()
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


}
package sjj.novel.data.source.remote

import io.reactivex.Observable
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import sjj.alog.Log
import sjj.novel.data.repository.NovelDataRepository
import sjj.novel.data.source.remote.rule.BookParseRule
import sjj.novel.data.source.remote.rule.Method
import sjj.novel.model.Book
import sjj.novel.model.Chapter
import java.net.URLEncoder

class CommonBookEngine(val rule: BookParseRule) : NovelDataRepository.RemoteSource, HttpDataSource() {

    override val baseUrl: String = rule.baseUrl

    private val service by lazy { create<HttpInterface>() }

    override val topLevelDomain: String = rule.topLevelDomain

    override fun search(search: String): Observable<List<Book>> {
        return Observable.just(rule).flatMap {
            Log.i("search searchRule")
            val searchRule = it.searchRule!!
            Log.i("search Encode Parameter")
            val parameter = mutableMapOf<String, String>()
            parameter[searchRule.searchKey] = URLEncoder.encode(search, searchRule.charset.name)
            Log.i("search ${searchRule.method}")
            if (searchRule.method == Method.GET) {
                return@flatMap service.searchGet(searchRule.serverUrl, parameter)
            } else {
                return@flatMap service.searchPost(searchRule.serverUrl, parameter)
            }
        }.map { response ->
            Log.i("search parse html")
            val document = Jsoup.parse(response.body(), response.baseUrl)
            val resultRules = rule.searchRule!!.resultRules!!
            Log.i("search foreach searchResultSize:"+resultRules.size)
            resultRules.forEach { resultRule ->
                val elements = document.select(resultRule.bookInfos)
                Log.i("search foreach books "+elements.size)
                val books = mutableListOf<Book>()
                elements.forEach { element ->

                    val bookName = element.select(resultRule.name).text(resultRule.nameRegex).trim()
                    Log.i("search bookAuthor bookName:$bookName")
                    val bookAuthor = element.select(resultRule.author).text(resultRule.authorRegex).trim()
                    Log.i("search bookUrl BookAuthor:$bookAuthor")
                    val bookUrl = element.absUrl(resultRule.bookUrl, response).trim()
                    Log.i("search new Book bookUrl:$bookUrl")
                    if (bookName.isNotBlank() && bookAuthor.isNotBlank() && bookUrl.isNotBlank()) {
                        books.add(Book(bookUrl, bookName, bookAuthor))
                    }
                }
                if (books.isNotEmpty()) {
                    return@map books
                }
            }
            return@map listOf<Book>()
        }
    }


    override fun getBook(url: String): Observable<Book> {
        return service.loadHtml(url).map { response ->
            Log.i("getBook parse")
            val document = Jsoup.parse(response.body(), response.baseUrl)
            val introRule = rule.introRule!!
            Log.i("getBook bookUrl introRule:$introRule")
            val bookUrl = document.absUrl(introRule.bookUrl, response).trim()
            Log.i("getBook bookName bookUrl:$bookUrl")
            val bookName = document.select(introRule.bookName).text(introRule.bookNameRegex).trim()
            Log.i("getBook bookAuthor bookName:$bookName")
            val bookAuthor = document.select(introRule.bookAuthor).text(introRule.bookAuthorRegex).trim()
            Log.i("getBook bookCoverSrc bookAuthor:$bookAuthor")
            val bookCoverSrc = document.select(introRule.bookCoverImgUrl).first().absUrl("src").trim()
            Log.i("getBook bookIntro bookCoverSrc:$bookCoverSrc")
            val bookIntro = document.select(introRule.bookIntro).text(introRule.bookIntroRegex)
            Log.i("getBook bookChapterList bookIntro:$bookIntro")
            val bookChapterListUrl = document.absUrl(introRule.bookChapterListUrl, response).trim()
            Log.i("getBook book bookChapterListUrl:$bookChapterListUrl")
            val book = Book(bookUrl, bookName, bookAuthor, bookCoverSrc, bookIntro, bookChapterListUrl)
            Log.i("getBook chapterList bookChapterListUrl == bookUrl:${bookChapterListUrl == bookUrl}")
            if (bookChapterListUrl == bookUrl) {
                //如果完整的章节列表与简介在同一页
                Log.i("getBook parseBookChapterList")
                book.chapterList = parseBookChapterList(document, book)
            }
            book
        }.flatMap { book ->
            //如果完整的章节列表与简介不在同一页在加载章节列表
            if (book.chapterListUrl != book.url) {
                Log.i("getBook loadHtml chapterListUrl")
                service.loadHtml(book.chapterListUrl).map {
                    val document = Jsoup.parse(it.body(), it.baseUrl)
                    Log.i("getBook parseBookChapterList")
                    book.chapterList = parseBookChapterList(document, book)
                    book
                }
            } else {
                Observable.just(book)
            }

        }
    }

    private fun parseBookChapterList(element: Element, book: Book): List<Chapter> {
        val chapterListRule = rule.chapterListRule!!
        val chapters = mutableListOf<Chapter>()
        Log.i("getBook foreach bookChapterList")
        element.select(chapterListRule.bookChapterList).forEachIndexed { index, e ->
            val url = e.select(chapterListRule.bookChapterUrl).first().absUrl("href").trim()
            Log.i("getBook chapter name url:$url")
            val name = e.select(chapterListRule.bookChapterName).text(chapterListRule.bookChapterNameRegex).trim()
            Log.i("getBook chapter name:$name")
            chapters.add(Chapter(url, book.url, index, name))
        }
        Log.i("getBook chapters chapters size:${chapters.size}")
        return chapters

    }

    override fun getChapterContent(chapter: Chapter): Observable<Chapter> {
        return service.loadHtml(chapter.url).map {
            val document = Jsoup.parse(it.body(), it.baseUrl)
            chapter.content = document.select(rule.chapterContentRule!!.bookChapterContent).html()
            chapter.isLoadSuccess = true
            chapter
        }
    }

}
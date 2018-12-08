package sjj.novel.data.source.remote

import io.reactivex.Observable
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import sjj.novel.data.repository.NovelDataRepository
import sjj.novel.data.source.remote.rule.BookParseRule
import sjj.novel.data.source.remote.rule.Method
import sjj.novel.model.Book
import sjj.novel.model.Chapter
import java.net.URLEncoder

class CommonBookEngine(private val rule: BookParseRule) : NovelDataRepository.RemoteSource, HttpDataSource() {

    override val baseUrl: String = rule.baseUrl

    private val service by lazy { create<HttpInterface>() }

    override val topLevelDomain: String = rule.topLevelDomain

    override fun search(search: String): Observable<List<Book>> {
        return Observable.just(rule).flatMap {
            val searchRule = it.searchRule!!
            val parameter = mutableMapOf<String, String>()
            parameter[searchRule.searchKey] = URLEncoder.encode(search, searchRule.charset.name)

            if (searchRule.method == Method.GET) {
                return@flatMap service.searchGet(searchRule.serverUrl, parameter)
            } else {
                return@flatMap service.searchPost(searchRule.serverUrl, parameter)
            }
        }.map { response ->
            val document = Jsoup.parse(response.body(), response.baseUrl)
            val resultRules = rule.searchRule!!.resultRules!!
            resultRules.forEach { resultRule ->
                val elements = document.select(resultRule.bookInfos)
                val books = mutableListOf<Book>()
                elements.forEach { element ->

                    val bookName = element.select(resultRule.name).text(resultRule.nameRegex)

                    val bookAuthor = element.select(resultRule.author).text(resultRule.authorRegex)

                    val bookUrl = element.absUrl(resultRule.bookUrl, response)

                    if (bookName.isNotBlank() || bookAuthor.isNotBlank() || bookUrl.isNotBlank()) {
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
            val document = Jsoup.parse(response.body(), response.baseUrl)
            val introRule = rule.introRule!!
            val bookInfo = document.select(introRule.bookInfo)
            val bookUrl = bookInfo.absUrl(introRule.bookUrl, response)
            val bookName = bookInfo.select(introRule.bookName).text(introRule.bookNameRegex)
            val bookAuthor = bookInfo.select(introRule.bookAuthor).text(introRule.bookAuthorRegex)
            val bookCoverSrc = bookInfo.select(introRule.bookCoverImgUrl).first().absUrl("src")
            val bookIntro = bookInfo.select(introRule.bookIntro).text(introRule.bookIntroRegex)
            val bookChapterListUrl = bookInfo.absUrl(introRule.bookChapterListUrl, response)
            val book = Book(bookUrl, bookName, bookAuthor, bookCoverSrc, bookIntro, bookChapterListUrl)
            if (bookChapterListUrl == bookUrl) {
                //如果完整的章节列表与简介在同一页
                book.chapterList = parseBookChapterList(document, book)
            }
            book
        }.flatMap { book ->
            //如果完整的章节列表与简介不在同一页在加载章节列表
            if (book.chapterListUrl != book.url) {
                service.loadHtml(book.chapterListUrl).map {
                    val document = Jsoup.parse(it.body(), it.baseUrl)
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
        element.select(chapterListRule.bookChapterList).forEachIndexed { index, e ->
            val url = e.select(chapterListRule.bookChapterUrl).first().absUrl("href")
            val name = e.select(chapterListRule.bookChapterName).text(chapterListRule.bookChapterNameRegex)
            chapters.add(Chapter(url, book.url, index, name))
        }
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
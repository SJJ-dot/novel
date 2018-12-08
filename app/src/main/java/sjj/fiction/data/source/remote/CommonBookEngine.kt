package sjj.fiction.data.source.remote

import io.reactivex.Observable
import org.jsoup.Jsoup
import sjj.alog.Log
import sjj.fiction.data.repository.FictionDataRepository
import sjj.fiction.data.source.remote.rule.BookParseRule
import sjj.fiction.data.source.remote.rule.Method
import sjj.fiction.model.Book
import sjj.fiction.model.Chapter
import java.lang.IllegalArgumentException
import java.net.URLEncoder

class CommonBookEngine(private val rule: BookParseRule) : FictionDataRepository.RemoteSource, HttpDataSource() {

    override val baseUrl: String = rule.baseUrl

    private val service by lazy { create<HttpInterface>() }

    override fun getChapterContent(chapter: Chapter): Observable<Chapter> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getBook(url: String): Observable<Book> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

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

                    val bookUrl = if (resultRule.bookUrl.isBlank()) {
                        response.baseUrl
                    } else {
                        element.select(resultRule.bookUrl).first()?.absUrl("href")
                                ?: response.baseUrl
                    }
                    element.select(resultRule.bookUrl).text()
                    if (bookName.isNotBlank() || bookAuthor.isNotBlank()) {
                        books.add(Book(bookUrl, bookName, bookAuthor))
                    }
                }
                if (books.isNotEmpty()) {
                    return@map books
                }
            }

            return@map listOf<Book>()
        }.doOnNext(Log::e)
    }
}
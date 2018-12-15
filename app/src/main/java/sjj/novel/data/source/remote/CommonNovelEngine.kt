package sjj.novel.data.source.remote

import io.reactivex.Observable
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import retrofit2.Response
import retrofit2.http.*
import sjj.alog.Log
import sjj.novel.data.repository.NovelDataRepository
import sjj.novel.data.source.remote.retrofit.Html
import sjj.novel.data.source.remote.rule.BookParseRule
import sjj.novel.data.source.remote.rule.Method
import sjj.novel.model.Book
import sjj.novel.model.Chapter
import java.net.URLEncoder

class CommonNovelEngine(val rule: BookParseRule) : NovelDataRepository.RemoteSource, HttpDataSource() {

    override val baseUrl: String = rule.baseUrl

    private val service by lazy { create<HttpInterface>() }

    override val topLevelDomain: String = rule.topLevelDomain

    override fun search(search: String): Observable<List<Book>> {
        return Observable.just(rule).flatMap {
            Log.i("搜索 搜索规则")
            val searchRule = it.searchRule!!
            Log.i("搜索 编码参数")
            val parameter = mutableMapOf<String, String>()
            parameter[searchRule.searchKey] = URLEncoder.encode(search, searchRule.charset.name)
            Log.i("搜索 请求方式：${searchRule.method}")
            if (searchRule.method == Method.GET) {
                return@flatMap service.searchGet(searchRule.serverUrl, parameter)
            } else {
                return@flatMap service.searchPost(searchRule.serverUrl, parameter)
            }
        }.map { response ->
            Log.i("搜索 解析html")
            val document = Jsoup.parse(response.body(), response.baseUrl)
            val resultRules = rule.searchRule!!.resultRules!!
            Log.i("搜索 遍历搜索结果解析规则列表:"+resultRules.size)
            resultRules.forEach { resultRule ->
                val elements = document.select(resultRule.bookInfos)
                Log.i("搜索 遍历搜索结果书籍信息列表： "+elements.size)
                val books = mutableListOf<Book>()
                elements.forEach { element ->

                    val bookName = element.select(resultRule.name).text(resultRule.nameRegex).trim()
                    Log.i("搜索 解析作者 书名:$bookName")
                    val bookAuthor = element.select(resultRule.author).text(resultRule.authorRegex).trim()
                    Log.i("搜索 书籍url 书籍作者:$bookAuthor")
                    val bookUrl = element.absUrl(resultRule.bookUrl, response).trim()
                    Log.i("搜索 创建书籍对象 bookUrl:$bookUrl")
                    if (bookName.isNotBlank() && bookAuthor.isNotBlank() && bookUrl.isNotBlank()) {
                        books.add(Book(bookUrl, bookName, bookAuthor))
                    }
                }
                if (books.isNotEmpty()) {
                    return@map books
                }
            }
            return@map listOf<Book>()
        }.doOnNext { books ->
            //搜索结果未加载详情
            books.forEach {
                it.loadStatus = Book.LoadState.UnLoad
            }
        }
    }


    override fun getBook(url: String): Observable<Book> {
        return service.loadHtml(url).map { response ->
            Log.i("详情 解析html")
            val document = Jsoup.parse(response.body(), response.baseUrl)
            val introRule = rule.introRule!!
            Log.i("详情 书籍url 简介规则:$introRule")
//            val bookUrl = document.absUrl(introRule.bookUrl, response).trim()
            val bookUrl = url
            Log.i("详情 书籍名 书籍url:$bookUrl")
            val bookName = document.select(introRule.bookName).text(introRule.bookNameRegex).trim()
            Log.i("详情 作者 书名:$bookName")
            val bookAuthor = document.select(introRule.bookAuthor).text(introRule.bookAuthorRegex).trim()
            Log.i("详情 封面url 作者:$bookAuthor")
            val bookCoverSrc = document.select(introRule.bookCoverImgUrl).first().absUrl("src").trim()
            Log.i("详情 简介 封面url:$bookCoverSrc")
            val bookIntro = document.select(introRule.bookIntro).text(introRule.bookIntroRegex)
            Log.i("详情 章节列表url 简介:$bookIntro")
            val bookChapterListUrl = document.absUrl(introRule.bookChapterListUrl, response).trim()
            Log.i("详情 创建书籍对象 章节列表url:$bookChapterListUrl")
            val book = Book(bookUrl, bookName, bookAuthor, bookCoverSrc, bookIntro, bookChapterListUrl)
            Log.i("详情 章节列表 章节列表的url == 书籍url:${bookChapterListUrl == bookUrl}")
            if (bookChapterListUrl == bookUrl) {
                //如果完整的章节列表与简介在同一页
                Log.i("详情 解析章节列表")
                book.chapterList = parseBookChapterList(document, book)
            }
            book
        }.flatMap { book ->
            //如果完整的章节列表与简介不在同一页在加载章节列表
            if (book.chapterListUrl != book.url) {
                Log.i("详情 加载章节网页")
                service.loadHtml(book.chapterListUrl).map {
                    val document = Jsoup.parse(it.body(), it.baseUrl)
                    Log.i("详情 解析章节列表")
                    book.chapterList = parseBookChapterList(document, book)
                    book
                }
            } else {
                Observable.just(book)
            }

        }.doOnNext {
            it.loadStatus = Book.LoadState.Loaded
        }
    }

    private fun parseBookChapterList(element: Element, book: Book): List<Chapter> {
        val chapterListRule = rule.chapterListRule!!
        val chapters = mutableListOf<Chapter>()
        Log.i("详情 遍历章节元素")
        element.select(chapterListRule.bookChapterList).forEachIndexed { index, e ->
            val url = e.select(chapterListRule.bookChapterUrl).first().absUrl("href").trim()
            Log.i("详情 章节名 url:$url")
            val name = e.select(chapterListRule.bookChapterName).text(chapterListRule.bookChapterNameRegex).trim()
            Log.i("详情 创建章节对象 章节名:$name")
            chapters.add(Chapter(url, book.url, index, name))
        }
        Log.i("详情 章节列表解析完毕，章节数量:${chapters.size}")
        return chapters

    }

    override fun getChapterContent(chapter: Chapter): Observable<Chapter> {
        return service.loadHtml(chapter.url).map {
            Log.i("章节内容 解析html")
            val document = Jsoup.parse(it.body(), it.baseUrl)
            chapter.content = document.select(rule.chapterContentRule!!.bookChapterContent).html()
            Log.i("章节内容 获取到章节内容："+chapter.content)
            chapter.isLoadSuccess = true
            chapter
        }
    }
    interface HttpInterface {
        @FormUrlEncoded
        @POST
        @Html
        fun searchPost(@Url url: String, @FieldMap(encoded = true) map: Map<String, String>): Observable<Response<String>>

        @GET
        @Html
        fun searchGet(@Url url: String, @QueryMap(encoded = true) map: Map<String, String>): Observable<Response<String>>

        @GET
        @Html
        fun loadHtml(@Url url: String): Observable<Response<String>>

    }
}
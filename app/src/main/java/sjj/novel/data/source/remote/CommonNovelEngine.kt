package sjj.novel.data.source.remote

import io.reactivex.Observable
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
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
import kotlin.math.min

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
            val list = searchRule.searchKey.split("&")
            if (list.size > 1) {
                list.forEach {
                    val kv = it.split("=")
                    if (kv.size == 1) {
                        parameter[kv[0]] = URLEncoder.encode(search, searchRule.charset.name)
                    } else {
                        parameter[kv[0]] = URLEncoder.encode(kv[1], searchRule.charset.name)
                    }
                }
            }
            parameter[searchRule.searchKey] = URLEncoder.encode(search, searchRule.charset.name)
            Log.i("搜索 请求方式：${searchRule.method}")
            if (searchRule.method == Method.GET) {
                return@flatMap service.searchGet(searchRule.serverUrl, parameter)
            } else {
                return@flatMap service.searchPost(searchRule.serverUrl, parameter)
            }
        }.map { response ->
            Log.i("搜索 解析html:${response.body()}")
            val document = Jsoup.parse(response.body(), response.baseUrl)
            val resultRules = rule.searchRule!!.resultRules!!
            Log.i("搜索 遍历搜索结果解析规则列表:" + resultRules.size)
            resultRules.forEach { resultRule ->

                val result = Regex("(.*)\\[(\\d*+):(\\d*+)]").find(resultRule.bookInfos)
                val elements: Elements = if (result != null) {
                    val select = document.select(result.groupValues[1])
                    if (select.size <= 1) {
                        select
                    } else {
                        val start = ((result.groupValues[2].toIntOrNull() ?: 0) + select.size) % select.size
                        val end = result.groupValues[3].toIntOrNull()?.plus(select.size)?.rem(select.size) ?: select.size
                        Elements(select.subList(start, end))
                    }
                } else {
                    document.select(resultRule.bookInfos)
                }

                Log.i("搜索 遍历搜索结果书籍信息列表： " + elements.size)
                val books = mutableListOf<Book>()
                elements.forEach { element ->
                    val bookName = element.select(resultRule.name).text(resultRule.nameRegex).trim()
                    Log.i("搜索 解析作者 书名:$bookName")
                    val bookAuthor = element.select(resultRule.author).text(resultRule.authorRegex).trim()
                    Log.i("搜索 书籍url 书籍作者:$bookAuthor")
                    val bookUrl = element.absUrl(resultRule.bookUrl, response).trim()
                    Log.i("搜索 创建书籍对象 bookUrl:$bookUrl")

                    if (bookName.isNotBlank() && bookAuthor.isNotBlank() && bookUrl.isNotBlank()) {
                        val book = Book(bookUrl, bookName, bookAuthor)
                        books.add(book)
                        Log.i("搜索 解析最新章节 lastChapterUrl:${resultRule.lastChapterUrl}")
                        if (resultRule.lastChapterUrl.isNotBlank()) {
                            val lastChapterUrl = element.select(resultRule.lastChapterUrl).first()?.absUrl(if (resultRule.lastChapterUrl.contains("meta[")) "content" else "href")?.trim()
                            val chapterName = element.select(resultRule.lastChapterName).text(resultRule.lastChapterNameRegex)
                            Log.i("搜索 lastChapterUrl：$lastChapterUrl chapterName:$chapterName")
                            if (lastChapterUrl?.isNotBlank() == true && chapterName.isNotBlank()) {
                                book.lastChapter = Chapter(lastChapterUrl, bookUrl, chapterName = chapterName)
                            }
                        }
                        Log.i("搜索 解析封面 bookCoverImgUrl:${resultRule.bookCoverImgUrl}")
                        if (resultRule.bookCoverImgUrl.isNotBlank()) {
                            val trim = element.select(resultRule.bookCoverImgUrl).first()?.absUrl(if (resultRule.bookCoverImgUrl.contains("meta[")) "content" else "src")?.trim()
                            Log.i("搜索 bookCoverImgUrl：$trim")
                            if (trim?.isNotBlank() == true) {
                                book.bookCoverImgUrl = trim
                            }
                        }
                    }
                }
                if (books.isNotEmpty()) {
                    return@map books
                }
            }
            Log.i("搜索 未搜索到结果 ${rule.sourceName}")
            return@map listOf<Book>()
        }.doOnNext { books ->
            //搜索结果未加载详情
            books.forEach {
                it.origin = rule
                it.loadStatus = Book.LoadState.UnLoad
            }
        }.doOnError {
            Log.e("搜索 搜索出错 ${rule.sourceName} ${it.message}", it)
        }
    }


    override fun getBook(url: String): Observable<Book> {
        return service.loadHtml(url).map { response ->
            Log.i("详情 解析html:${response.body()}")
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
            val bookCoverSrc = document.select(introRule.bookCoverImgUrl).first().absUrl(if (introRule.bookCoverImgUrl.contains("meta[")) "content" else "src").trim()
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
            it.origin = rule
            it.loadStatus = Book.LoadState.Loaded
        }.doOnError {
            Log.e("详情 加载出错 ${rule.sourceName} ${it.message}", it)
        }
    }

    private fun parseBookChapterList(element: Element, book: Book): List<Chapter> {
        val chapterListRule = rule.chapterListRule!!
        val chapters = mutableListOf<Chapter>()
        Log.i("详情 遍历章节元素")
        val result = Regex("(.*)\\[(\\d*+):(\\d*+)]").find(chapterListRule.bookChapterList)

        val elements: Elements
        if (result != null) {
            val select = element.select(result.groupValues[1])
            val start = ((result.groupValues[2].toIntOrNull() ?: 0) + select.size) % select.size
            val end = result.groupValues[3].toIntOrNull()?.plus(select.size)?.rem(select.size) ?: select.size
            elements = Elements(select.subList(start, end))
        } else {
            elements = element.select(chapterListRule.bookChapterList)
        }

        elements.forEachIndexed { index, e ->
            val url = e.select(chapterListRule.bookChapterUrl).first().absUrl(if (chapterListRule.bookChapterUrl.contains("meta[")) "content" else "href").trim()
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
            Log.i("章节内容 解析html:${it.body()}")
            val document = Jsoup.parse(it.body(), it.baseUrl)
            chapter.content = document.select(rule.chapterContentRule!!.bookChapterContent).html()
            Log.i("章节内容 获取到章节内容：" + chapter.content)
            chapter.isLoadSuccess = true
            chapter
        }.doOnError {
            Log.e("章节内容 出错 ${rule.sourceName} ${it.message}", it)
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
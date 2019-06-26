package sjj.novel.data.source.remote

import io.reactivex.Observable
import retrofit2.Response
import retrofit2.http.*
import sjj.alog.Log
import sjj.novel.data.repository.NovelDataRepository
import sjj.novel.data.source.remote.parser.CssQueryNovelParser
import sjj.novel.data.source.remote.parser.JavaScriptNovelParser
import sjj.novel.data.source.remote.retrofit.ann.Html
import sjj.novel.data.source.remote.rule.BookParseRule
import sjj.novel.data.source.remote.rule.Method
import sjj.novel.model.Book
import sjj.novel.model.Chapter
import java.net.URLEncoder

class CommonNovelEngine(val rule: BookParseRule) : NovelDataRepository.RemoteSource, HttpDataSource() {

    override val baseUrl: String = rule.baseUrl

    private val service by lazy { create<HttpInterface>() }

    override val topLevelDomain: String = rule.topLevelDomain

    private val parser: NovelParser = when (rule.version) {
        BookParseRule.VERSION_JAVA_SCRIPT -> JavaScriptNovelParser(rule)
        else -> CssQueryNovelParser(rule)
    }

    override fun search(search: String): Observable<List<Book>> {
        return Observable.just(rule).flatMap {
            Log.i("搜索 搜索规则")
            val searchRule = it.searchRule!!
            Log.i("搜索 编码参数 ${searchRule.charset.name}")
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
            } else {
                parameter[searchRule.searchKey] = URLEncoder.encode(search, searchRule.charset.name)
            }
            Log.i("搜索 请求方式：${searchRule.method}")
            if (searchRule.method == Method.GET) {
                return@flatMap service.searchGet(searchRule.serverUrl, parameter)
            } else {
                return@flatMap service.searchPost(searchRule.serverUrl, parameter)
            }
        }.map { response ->
            parser.parseSearch(search,response)
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
            parser.parseBook(url, response)
        }.flatMap { book ->
            //如果完整的章节列表与简介不在同一页在加载章节列表
            if (book.chapterListUrl != book.url) {
                Log.i("详情 加载章节网页")
                service.loadHtml(book.chapterListUrl).map {
                    parser.parseBookChapterList(it,book)
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

    override fun getChapterContent(chapter: Chapter): Observable<Chapter> {
        return service.loadHtml(chapter.url).map {
            parser.parseChapterContent(chapter,it)
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

    interface NovelParser {
        fun parseChapterContent(chapter: Chapter, response: Response<String>): Chapter
        fun parseBookChapterList(response: Response<String>, book: Book): Book
        fun parseBook(url: String, response: Response<String>): Book
        fun parseSearch(search: String, response: Response<String>): List<Book>
    }
}
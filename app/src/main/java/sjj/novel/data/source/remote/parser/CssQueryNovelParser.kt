package sjj.novel.data.source.remote.parser

import android.text.Html
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import retrofit2.Response
import sjj.alog.Log
import sjj.novel.data.source.remote.CommonNovelEngine
import sjj.novel.data.source.remote.rule.BookParseRule
import sjj.novel.model.Book
import sjj.novel.model.Chapter

class CssQueryNovelParser(private val rule: BookParseRule) : CommonNovelEngine.NovelParser {
    override fun parseChapterContent(chapter: Chapter, response: Response<String>): Chapter {
        Log.i("章节内容 解析html")
        val document = Jsoup.parse(response.body(), response.baseUrl)
        chapter.content = document.select(rule.chapterContentRule!!.bookChapterContent).html()
        Log.i("章节内容 获取到章节内容：" + chapter.content)
        chapter.isLoadSuccess = true
        return chapter
    }

    override fun parseBookChapterList(response: Response<String>, book: Book): Book {
        val element = Jsoup.parse(response.body(), response.baseUrl)
        val chapterListRule = rule.chapterListRule!!
        val chapters = mutableListOf<Chapter>()
        Log.i("详情 遍历章节元素")
        val result = Regex("(.*)\\[(\\d*+):(\\d*+)]").find(chapterListRule.bookChapterList)

        val elements: Elements
        if (result != null) {
            val select = element.select(result.groupValues[1])
            val start = ((result.groupValues[2].toIntOrNull() ?: 0) + select.size) % select.size
            val end = result.groupValues[3].toIntOrNull()?.plus(select.size)?.rem(select.size)
                    ?: select.size
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
        book.chapterList = chapters
        return book
    }

    override fun parseBook(url: String, response: Response<String>): Book {
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
            parseBookChapterList(response, book)
        }
        return book
    }

    override fun parseSearch(search: String, response: Response<String>): List<Book> {
        Log.i("搜索 解析html")
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
                    val start = ((result.groupValues[2].toIntOrNull()
                            ?: 0) + select.size) % select.size
                    val end = result.groupValues[3].toIntOrNull()?.plus(select.size)?.rem(select.size)
                            ?: select.size
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
                return books
            }
        }
        Log.i("搜索 未搜索到结果 ${rule.sourceName}")
        return listOf()
    }


    /**
     * 获取 html 网页meta 属性值
     */
    protected fun Document.metaProp(attrValue: String): String {
        return getElementsByAttributeValue("property", attrValue)[0].attr("content")
    }

    /**
     * 获取请求的baseurl 不能为空
     */
    val Response<*>.baseUrl: String
        get() {
            var baseUrl = raw()?.networkResponse()?.request()?.url()?.toString()
            if (baseUrl.isNullOrBlank()) {
                baseUrl = raw()?.request()?.url()?.toString()
            }
            return baseUrl!!
        }

    /**
     * 通过给定的正则表达式匹配输出 第一个元组
     */
    fun Elements.text(regex: String): String {
        val attr = attr("content")
        if (attr.isNotEmpty()) {
            return attr
        }
        val text = html()
        //如果没有正则表达式设置 直接返回文本
        if (regex.isEmpty()) {
            return Html.fromHtml(text).toString()
        }
        return try {
            val result = Regex(regex).find(text)
            val trim = result!!.groups[1]!!.value
            Html.fromHtml(trim).toString()
        } catch (e: Exception) {
            Html.fromHtml(text).toString()
        }
    }

    /**
     *只有在url 可能为网页本身的时候才使用这个方法
     */
    fun Element.absUrl(cssQuery: String, response: Response<String>): String {
        return if (cssQuery.isBlank()) {
            response.baseUrl
        } else {
            val elements = select(cssQuery).first()
            if (cssQuery.contains("meta[")) {
                val attr = elements?.attr("content")
                if (attr?.isNotEmpty() == true) {
                    return attr
                }
            }
            elements?.absUrl("href") ?: response.baseUrl
        }
    }

    /**
     *只有在url 可能为网页本身的时候才使用这个方法
     */
    fun Elements.absUrl(cssQuery: String, response: Response<String>): String {
        return if (cssQuery.isBlank()) {
            response.baseUrl
        } else {
            select(cssQuery).first()?.absUrl("href") ?: response.baseUrl
        }
    }
}
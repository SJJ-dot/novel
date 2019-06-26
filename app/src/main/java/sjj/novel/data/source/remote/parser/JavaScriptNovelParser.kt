package sjj.novel.data.source.remote.parser

import retrofit2.Response
import sjj.novel.data.source.remote.CommonNovelEngine
import sjj.novel.data.source.remote.rule.BookParseRule
import sjj.novel.model.Book
import sjj.novel.model.Chapter

class JavaScriptNovelParser(private val rule: BookParseRule) : CommonNovelEngine.NovelParser {
    override fun parseChapterContent(chapter: Chapter, response: Response<String>): Chapter {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun parseBookChapterList(response: Response<String>, book: Book): Book {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun parseBook(url: String, response: Response<String>): Book {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun parseSearch(search: String, response: Response<String>): List<Book> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
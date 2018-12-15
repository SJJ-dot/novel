package sjj.novel.data.source.remote

import io.reactivex.Observable
import retrofit2.http.GET
import sjj.novel.data.repository.NovelSourceRepository
import sjj.novel.data.source.remote.rule.BookParseRule

val defaultNovelSource by lazy { DefaultNovelSource() }

class DefaultNovelSource : HttpDataSource() {

    override val baseUrl: String
        get() = "https://raw.githubusercontent.com"
    private val server by lazy { create<Interface>() }

    fun getDefaultNovelSourceRule(): Observable<List<BookParseRule>> {
        return server.getDefaultNovelSourceRule()
    }

    fun getNovelSourceRuleExplanation(): Observable<String> {
        return server.getNovelSourceRuleExplanation().flatMap {
            githubDataSource.getMarkDownHtml(it)
        }
    }

    interface Interface {
        @GET("https://raw.githubusercontent.com/lTBeL/novel_extra/master/novelRule/NovelSource.json")
        fun getDefaultNovelSourceRule(): Observable<List<BookParseRule>>

        @GET("https://raw.githubusercontent.com/lTBeL/novel_extra/master/doc/NovelSourceRuleExplanation.md")
        fun getNovelSourceRuleExplanation(): Observable<String>
    }
}
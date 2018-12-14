package sjj.novel.data.source.remote

import io.reactivex.Observable
import retrofit2.http.GET
import sjj.novel.data.repository.NovelSourceRepository
import sjj.novel.data.source.remote.rule.BookParseRule

class DefaultNovelSource : HttpDataSource(), NovelSourceRepository.RemoteSource {

    override val baseUrl: String
        get() = "https://raw.githubusercontent.com"
    private val server by lazy { create<Interface>() }

    override fun getDefaultNovelSourceRule(): Observable<List<BookParseRule>> {
        return server.getDefaultNovelSourceRule()
    }

    interface Interface {
        @GET("https://raw.githubusercontent.com/lTBeL/novel_extra/master/novelRule/NovelSource.json")
        fun getDefaultNovelSourceRule(): Observable<List<BookParseRule>>
    }

}
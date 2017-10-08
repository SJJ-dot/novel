package sjj.fiction.data.source.remote

import io.reactivex.Observable
import sjj.alog.Log
import sjj.fiction.data.Repository.SoduDataRepository
import sjj.fiction.data.service.SoduService
import sjj.fiction.model.SearchResultBook
import sjj.fiction.model.Url
import java.net.URLEncoder
import java.nio.charset.Charset

/**
 * Created by SJJ on 2017/9/3.
 */
class SoduDataSource : HttpDataSource(), SoduDataRepository.Source {
    private val service = create(SoduService::class.java)

    override fun search(search: String): Observable<SearchResultBook> {
        return service.search(URLEncoder.encode("极道天魔", "gb2312"))
                .map {
                    Log.e(it)
                    SearchResultBook("aaa", Url("http://www.sodu.cc/mulu_450287.html"))
                }
    }

}
package sjj.fiction.data.source.remote

import sjj.alog.Log
import sjj.fiction.data.Repository.SoduDataRepository
import sjj.fiction.data.service.SoduService
import java.net.URLEncoder
import java.nio.charset.Charset

/**
 * Created by SJJ on 2017/9/3.
 */
class SoduDataSource : HttpDataSource(), SoduDataRepository.Source {
    private val service = create(SoduService::class.java)
    override fun search(search: String): String {
        service.search(URLEncoder.encode("极道天魔","gb2312"))
                .subscribe({ Log.e( it) }, { Log.e("error", it) })
        return ""
    }

    override fun destroy() {


    }

}
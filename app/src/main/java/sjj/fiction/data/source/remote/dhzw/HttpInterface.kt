package sjj.fiction.data.source.remote.dhzw

import io.reactivex.Observable
import retrofit2.http.*
import sjj.fiction.data.source.remote.CHARSET

/**
 * Created by sjj on 2017/10/9.
 */
interface HttpInterface {
    @FormUrlEncoded
    @POST("modules/article/search.php")
    @CHARSET("gbk")
    fun search(@Field("searchkey",encoded = true) value: String): Observable<String>
}
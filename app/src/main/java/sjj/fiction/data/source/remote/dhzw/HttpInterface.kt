package sjj.fiction.data.source.remote.dhzw

import io.reactivex.Observable
import retrofit2.http.*

/**
 * Created by sjj on 2017/10/9.
 */
interface HttpInterface {
    @FormUrlEncoded
    @POST("modules/article/search.php")
    fun search(@Field("searchkey",encoded = true) value: String): Observable<String>
}
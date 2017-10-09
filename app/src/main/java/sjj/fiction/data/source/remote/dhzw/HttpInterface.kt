package sjj.fiction.data.source.remote.dhzw

import io.reactivex.Observable
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Created by sjj on 2017/10/9.
 */
interface HttpInterface {
    @FormUrlEncoded
    @POST("modules/article/search.php")
    fun search(@Field("searchkey") value: String): Observable<String>
}
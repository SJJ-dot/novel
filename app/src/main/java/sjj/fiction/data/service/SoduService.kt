package sjj.fiction.data.service

import android.text.Html
import io.reactivex.Observable
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Created by SJJ on 2017/9/3.
 */
interface SoduService {
    @GET("/search/index.aspx")
    fun search(@Query("key",encoded = true) value: String): Observable<List<String>>;
}
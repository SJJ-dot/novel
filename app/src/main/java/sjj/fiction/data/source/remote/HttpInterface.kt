package sjj.fiction.data.source.remote

import io.reactivex.Observable
import retrofit2.http.*

/**
 * Created by sjj on 2017/10/9.
 */
interface HttpInterface {
    @FormUrlEncoded
    @POST
    @Html
    fun searchPost(@Url url: String, @FieldMap(encoded = true) map: Map<String, String>): Observable<String>

    @GET
    @Html
    fun searchGet(@Url url: String, @QueryMap map: Map<String, String>): Observable<String>

    @GET
    @Html
    fun loadHtml(@Url url: String): Observable<String>

}
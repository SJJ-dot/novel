package sjj.novel.data.source.remote

import io.reactivex.Observable
import retrofit2.Response
import retrofit2.http.*
import sjj.novel.data.source.remote.retrofit.Html

/**
 * Created by sjj on 2017/10/9.
 */
interface HttpInterface {
    @FormUrlEncoded
    @POST
    @Html
    fun searchPost(@Url url: String, @FieldMap(encoded = true) map: Map<String, String>): Observable<Response<String>>

    @GET
    @Html
    fun searchGet(@Url url: String, @QueryMap(encoded = true) map: Map<String, String>): Observable<Response<String>>

    @GET
    @Html
    fun loadHtml(@Url url: String): Observable<Response<String>>

}
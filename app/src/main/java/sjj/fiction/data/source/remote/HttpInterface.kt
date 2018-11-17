package sjj.fiction.data.source.remote

import io.reactivex.Observable
import retrofit2.http.*
import sjj.fiction.data.source.remote.CHARSET

/**
 * Created by sjj on 2017/10/9.
 */
interface HttpInterface {
    @FormUrlEncoded
    @CHARSET("gbk")
    @POST
    fun searchForGBK(@Url url: String, @FieldMap(encoded = true) map: Map<String, String>): Observable<String>

    @CHARSET("gbk")
    @GET
    fun searchForGBKGET(@Url url: String, @QueryMap map: Map<String, String>): Observable<String>

    @GET
    fun searchForGET(@Url url: String, @QueryMap map: Map<String, String>): Observable<String>

    @GET
    @CHARSET("gbk")
    fun loadHtmlForGBK(@Url url: String): Observable<String>

    @GET
    fun loadHtml(@Url url: String): Observable<String>

}
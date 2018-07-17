package sjj.fiction.data.source.remote

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import okio.Buffer
import org.jsoup.nodes.Document
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import sjj.alog.Log
import sjj.fiction.App
import sjj.fiction.data.source.DataSourceInterface
import java.io.EOFException
import java.lang.reflect.Type
import java.net.HttpURLConnection
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit

/**
 * Created by SJJ on 2017/9/3.
 */
abstract class HttpDataSource : DataSourceInterface {
    abstract val baseUrl: String

    protected val retrofit: Retrofit by lazy {
        Retrofit.Builder()
                .client(client)
                .baseUrl(baseUrl)
                .addConverterFactory(CharsetStringConverterFactory())
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
//                .addCallAdapterFactory(ObserveOnMainCallAdapterFactory())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .build()
    }
    protected inline fun <reified T> create(): T {
        return retrofit.create(T::class.java)
    }

    private class CharsetStringConverterFactory : Converter.Factory() {
        private val gson = Gson()
        override fun responseBodyConverter(type: Type?, annotations: Array<out Annotation>?, retrofit: Retrofit?): Converter<ResponseBody, *>? {
            val charset = annotations?.find { it is CHARSET } as? CHARSET
            if (charset != null)
                return if (type == String::class.java) {
                    Converter<ResponseBody, String> {
                        responseCharset(it, charset)
                    }
                } else {
                    val adapter = gson.getAdapter(TypeToken.get(type))
                    Converter<ResponseBody, Any> {
                        adapter.fromJson(responseCharset(it, charset))
                    }
                }
            return null
        }

        fun responseCharset(responseBody: ResponseBody, charset: CHARSET) = responseBody.bytes().toString(kotlin.text.charset(charset.charset))

    }

    private class ObserveOnMainCallAdapterFactory : CallAdapter.Factory() {
        val scheduler = AndroidSchedulers.mainThread()!!
        val io = Schedulers.computation()

        override fun get(returnType: Type, annotations: Array<Annotation>, retrofit: Retrofit): CallAdapter<*, *>? {
            if (CallAdapter.Factory.getRawType(returnType) != Observable::class.java) {
                return null // Ignore non-Observable types.
            }
            // Look up the next call adapter which would otherwise be used if this one was not present.

            val delegate = retrofit.nextCallAdapter(this, returnType, annotations) as CallAdapter<Any, Observable<*>>

            return object : CallAdapter<Any, Any> {
                override fun adapt(call: Call<Any>): Any {
                    // Delegate to get the normal Observable...
                    val o = delegate.adapt(call)
                    // ...and change it to send notifications to the observer on the specified scheduler.
                    return o.subscribeOn(io)
                }

                override fun responseType(): Type {
                    return delegate.responseType()
                }
            }
        }
    }
    protected fun Document.metaProp(attrValue: String): String {
        return getElementsByAttributeValue("property", attrValue)[0].attr("content")
    }
}

private val client by lazy {
    OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
//            .addNetworkInterceptor {
//                val response = it.proceed(it.request())
//                if (response.code() == HttpURLConnection.HTTP_MOVED_TEMP) {
//                    val buffer = Buffer()
//                    it.request().body()?.writeTo(buffer)
//                    val utf8 = Charset.forName("UTF-8")
//                    val charset: Charset = it.request().body()?.contentType()?.charset(utf8) ?: utf8
//                    val body = if (isPlaintext(buffer)) buffer.readString(charset) else ""
//                    App.app.config.setHttp302Url(it.request().url().toString(), response.header("Location") ?: "", body)
//                }
//                response
//            }
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.HEADERS))
            .build()
}

internal fun isPlaintext(buffer: Buffer): Boolean {
    try {
        val prefix = Buffer()
        val byteCount = if (buffer.size() < 64) buffer.size() else 64
        buffer.copyTo(prefix, 0, byteCount)
        for (i in 0..15) {
            if (prefix.exhausted()) {
                break
            }
            val codePoint = prefix.readUtf8CodePoint()
            if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                return false
            }
        }
        return true
    } catch (e: EOFException) {
        return false // Truncated UTF-8 sequence.
    }

}
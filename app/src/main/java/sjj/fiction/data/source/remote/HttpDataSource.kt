package sjj.fiction.data.source.remote

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import sjj.alog.Log
import sjj.fiction.data.source.DataSourceInterface
import java.lang.reflect.Type
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit

/**
 * Created by SJJ on 2017/9/3.
 */
abstract class HttpDataSource : DataSourceInterface {
    abstract fun baseUrl(): String
    private fun retrofit(): Retrofit {
        return Retrofit.Builder()
                .client(client)
                .baseUrl(baseUrl())
                .addConverterFactory(CharsetStringConverterFactory())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(ObserveOnMainCallAdapterFactory())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
    }

    protected fun <T> create(service: Class<T>): T {
        return retrofit().create(service)
    }

    private class CharsetStringConverterFactory : Converter.Factory() {
        private val gson = Gson()
        override fun responseBodyConverter(type: Type?, annotations: Array<out Annotation>?, retrofit: Retrofit?): Converter<ResponseBody, *>? {
            val charset = annotations?.find { it is CHARSET } as? CHARSET
            if (charset != null)
                if (type == String::class.java) {
                    return Converter<ResponseBody, String> {
                        responseCharset(it,charset)
                    }
                } else {
                    val adapter = gson.getAdapter(TypeToken.get(type))
                    return Converter<ResponseBody,Any> {
                        adapter.fromJson(responseCharset(it,charset))
                    }
                }
            return null
        }

        fun responseCharset(responseBody: ResponseBody, charset: CHARSET) = responseBody.bytes().toString(kotlin.text.charset(charset.charset))

    }

    private class ObserveOnMainCallAdapterFactory : CallAdapter.Factory() {
        val scheduler = AndroidSchedulers.mainThread()
        val io = Schedulers.io()

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
                    return o.subscribeOn(io).unsubscribeOn(io).observeOn(scheduler)
                }

                override fun responseType(): Type {
                    return delegate.responseType()
                }
            }
        }
    }
}

private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        .build()
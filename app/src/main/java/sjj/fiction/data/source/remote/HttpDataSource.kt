package sjj.fiction.data.source.remote

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
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(ObserveOnMainCallAdapterFactory())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
    }

    protected fun <T> create(service: Class<T>): T {
        return retrofit().create(service)
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

    private class CharsetStringConverterFactory : Converter.Factory() {
        override fun responseBodyConverter(type: Type?, annotations: Array<out Annotation>?, retrofit: Retrofit?): Converter<ResponseBody, *>? {
            val find = annotations?.find { it is CHARSET }
            if (type == String::class.java && find != null) {
                return Converter<ResponseBody, String> {
                    find as CHARSET
                    it.bytes().toString(Charset.forName(find.charset))
                }
            }
            return null
        }
    }
}

private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        .build()
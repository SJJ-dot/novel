package sjj.novel.data.source.remote

import android.text.Html
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Response
import retrofit2.Retrofit
import sjj.novel.data.source.remote.retrofit.RetrofitInstance
import java.lang.reflect.Type

/**
 * Created by SJJ on 2017/9/3.
 */
abstract class HttpDataSource{
    abstract val baseUrl: String

    protected val retrofit: Retrofit by lazy {
        RetrofitInstance.defRetrofit(baseUrl)
    }

    protected inline fun <reified T> create(): T {
        return retrofit.create(T::class.java)
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


}




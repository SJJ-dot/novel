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

    /**
     * 获取 html 网页meta 属性值
     */
    protected fun Document.metaProp(attrValue: String): String {
        return getElementsByAttributeValue("property", attrValue)[0].attr("content")
    }

    /**
     * 获取请求的baseurl 不能为空
     */
    val Response<*>.baseUrl: String
        get() {
            var baseUrl = raw()?.networkResponse()?.request()?.url()?.toString()
            if (baseUrl.isNullOrBlank()) {
                baseUrl = raw()?.request()?.url()?.toString()
            }
            return baseUrl!!
        }

    /**
     * 通过给定的正则表达式匹配输出 第一个元组
     */
    fun Elements.text(regex: String): String {
        val text = html()
        //如果没有正则表达式设置 直接返回文本
        if (regex.isEmpty()) {
            return Html.fromHtml(text).toString()
        }
        return try {
            val result = Regex(regex).find(text)
            val trim = result!!.groups[1]!!.value
            Html.fromHtml(trim).toString()
        } catch (e: Exception) {
            Html.fromHtml(text).toString()
        }
    }

    /**
     *只有在url 可能为网页本身的时候才使用这个方法
     */
    fun Element.absUrl(cssQuery: String, response: Response<String>): String {
        return if (cssQuery.isBlank()) {
            response.baseUrl
        } else {
            select(cssQuery).first()?.absUrl("href") ?: response.baseUrl
        }
    }

    /**
     *只有在url 可能为网页本身的时候才使用这个方法
     */
    fun Elements.absUrl(cssQuery: String, response: Response<String>): String {
        return if (cssQuery.isBlank()) {
            response.baseUrl
        } else {
            select(cssQuery).first()?.absUrl("href") ?: response.baseUrl
        }
    }

    private fun getRoot(elements: Elements): Element {
        return getRoot(elements.first())
    }

    private fun getRoot(element: Element): Element {
        if (element is Document) {
            return element
        }
        return getRoot(element.parent())
    }
}




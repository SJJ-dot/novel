package sjj.novel.data.source.remote.retrofit

import com.facebook.stetho.okhttp3.StethoInterceptor
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okio.Buffer
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import sjj.novel.data.source.remote.retrofit.charset.HtmlEncodeConverter
import java.io.EOFException
import java.util.concurrent.TimeUnit
import java.util.Collections.singletonList
import okhttp3.CipherSuite
import okhttp3.TlsVersion
import okhttp3.ConnectionSpec
import java.util.*


object RetrofitInstance {

    fun defRetrofit(baseUrl: String): Retrofit {
        return Retrofit.Builder()
                .client(defOkHttpClient())
                .baseUrl(baseUrl)
                .addConverterFactory(HtmlEncodeConverter.create())
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
//                .addCallAdapterFactory(ObserveOnMainCallAdapterFactory())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .build()
    }

    fun defOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
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
//                .connectionSpecs(Collections.singletonList(ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
//                        .tlsVersions(TlsVersion.TLS_1_0, TlsVersion.TLS_1_1, TlsVersion.TLS_1_2)
//                        .cipherSuites(
//                                CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
//                                CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
//                                CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256,
//                                CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,
//                                CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,
//                                CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA,
//                                CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256)
//                        .build()))
                .addNetworkInterceptor(StethoInterceptor())
                .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .build()
    }

    fun isPlaintext(buffer: Buffer): Boolean {
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

}
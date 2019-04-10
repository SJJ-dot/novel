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
import sjj.alog.Log
import sjj.novel.R
import sjj.novel.Session
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.CertPathValidatorException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.*


object RetrofitInstance {

    fun defRetrofit(baseUrl: String): Retrofit {
        return Retrofit.Builder()
                .client(defOkHttpClient)
                .baseUrl(baseUrl)
                .addConverterFactory(HtmlEncodeConverter.create())
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
//                .addCallAdapterFactory(ObserveOnMainCallAdapterFactory())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .build()
    }

    val defOkHttpClient: OkHttpClient
        get() =  OkHttpClient.Builder()
                .setCertificate(R.raw.ssl_37shuwu)
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .addNetworkInterceptor(StethoInterceptor())
                .addInterceptor {
                    it.proceed(it.request().newBuilder()
                            .addHeader("Keep-Alive", "300")
                            .addHeader("Connection", "Keep-Alive")
                            .addHeader("Cache-Control", "no-cache")
                            .build())
                }
                .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                .build()


    fun OkHttpClient.Builder.setCertificate(vararg cerResID: Int): OkHttpClient.Builder {
        try {
            val certificateFactory = CertificateFactory.getInstance("X.509");
            val keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);

            cerResID.forEach {
                val inputStream = Session.ctx.resources.openRawResource(it)
                val ca = certificateFactory.generateCertificate(inputStream)
                keyStore.setCertificateEntry("ca $it", ca)
                inputStream.close();
            }

            val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            tmf.init(keyStore)

            val defFactory = TrustManagerFactory.getInstance("X509");
            defFactory.init(null as KeyStore?)
            val manager:X509TrustManager = object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<out X509Certificate>, authType: String) {
                    Log.i("checkClientTrusted")
                }

                override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
                    Log.i("checkServerTrusted")

//                    try {
//                        defFactory.getTrustManagers().forEach {
//                            it as X509TrustManager
//                            it.checkServerTrusted(chain, authType)
//                        }
//                    } catch (e: CertPathValidatorException) {
//                        Log.e("checkServer error",e)
//                        try {
//                            tmf.trustManagers.forEach {
//                                it as X509TrustManager
//                                it.checkServerTrusted(chain, authType)
//                            }
//                        } catch (e: CertPathValidatorException) {
//                            Log.e("checkServer error",e)
//                            throw e
//                        }
//                    }
                }

                override fun getAcceptedIssuers(): Array<X509Certificate> {
                    return arrayOf()
                }

            }


            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, arrayOf(manager), SecureRandom())
            sslSocketFactory(sslContext.socketFactory, manager);
//            hostnameVerifier { hostName, session ->
//                true
//            }
        } catch (e: Exception) {
            Log.e("setCertificate ERROR ", e)
        }
        return this
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
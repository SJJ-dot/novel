package sjj.novel

import android.app.Application
import android.os.Environment
import android.os.StrictMode
import com.halfhp.rxtracer.RxTracer
import com.tencent.bugly.Bugly
import io.reactivex.plugins.RxJavaPlugins
import sjj.alog.Config
import sjj.alog.Log
import java.io.File


/**
 * Created by SJJ on 2017/9/3.
 */
class App : Application() {
    companion object {
        lateinit var app: App
    }


    override fun onCreate() {
        super.onCreate()
        app = this
        Session.ctx = this

        Bugly.init(this, "6dbb38183e", BuildConfig.DEBUG)
        ActivityLifecycle.setActivityLifecycleCallback(this)

        // 分别为MainThread和VM设置Strict Mode
        if (BuildConfig.DEBUG && false) {
            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()

                    //                    .detectResourceMismatches()
                    .detectCustomSlowCalls().detectAll()
                    .penaltyLog()
//                    .penaltyDeath()
                    .build())

            StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .detectLeakedRegistrationObjects()
                    .detectActivityLeaks().detectAll()
                    .penaltyLog()
//                    .penaltyDeath()
                    .build())
        }

        RxJavaPlugins.setErrorHandler { Log.e("error $it", it) }

//        RxTracer.enable()

        Config.init(Config().apply {
            printAllLog = true
            hold = true
            val logDir = externalCacheDir ?: Environment.getExternalStorageDirectory() ?: cacheDir
            dir = File(logDir, "log")
        })
    }

    fun exit() {
        System.exit(0)
    }
}
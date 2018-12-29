package sjj.novel

import android.app.Application
import android.os.StrictMode
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.stetho.Stetho
import com.tencent.bugly.Bugly
import io.reactivex.plugins.RxJavaPlugins
import sjj.alog.Log


/**
 * Created by SJJ on 2017/9/3.
 */
class App:Application() {
    companion object {
        lateinit var app: App
    }


    override fun onCreate() {
        super.onCreate()
        app = this
        Session.ctx = this

        Bugly.init(this, "6dbb38183e", BuildConfig.DEBUG)
        ActivityLifecycle.setActivityLifecycleCallback(this)


        Fresco.initialize(Session.ctx);

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

        RxJavaPlugins.setErrorHandler { Log.e("error $it",it) }

        Stetho.initializeWithDefaults(this);

    }

    fun exit() {
        System.exit(0)
    }
}
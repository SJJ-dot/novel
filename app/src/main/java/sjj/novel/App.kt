package sjj.novel

import android.app.Application
import android.content.SharedPreferences
import android.os.StrictMode
import com.facebook.drawee.backends.pipeline.Fresco
import com.tencent.bugly.Bugly
import com.tencent.mmkv.MMKV
import io.reactivex.plugins.RxJavaPlugins
import sjj.alog.Log

/**
 * Created by SJJ on 2017/9/3.
 */
//@DefaultLifeCycle(application = "sjj.novel.App", flags = ShareConstants.TINKER_ENABLE_ALL, loadVerifyFlag = false)
class App:Application() {
    companion object {
        lateinit var app: App
    }


    override fun onCreate() {
        super.onCreate()
        app = this
        Session.ctx = this
        MMKV.initialize(this)
        Bugly.init(this, "6dbb38183e", BuildConfig.DEBUG)
        ActivityLifecycle.setActivityLifecycleCallback(this)

//        val logConfig = Config()
//        logConfig.hold = false
//        logConfig.holdMultiple = false
//        logConfig.holdLev = Config.ERROR
//        Config.init(logConfig)

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
    }

    fun exit() {
        System.exit(0)
    }
    //将旧版本的SharedPreferences中的数据迁移到 mmkv 中
    override fun getSharedPreferences(name: String, mode: Int): SharedPreferences {
        val set = AppConfig.migratedSharedPreferences
        val mmkv = MMKV.mmkvWithID("SharedPreferences_Migrated_$name", mode)
        if (!set.contains(name)) {
            set.add(name)
            AppConfig.migratedSharedPreferences = set
            val preferences = super.getSharedPreferences(name, mode)
            mmkv.importFromSharedPreferences(preferences)
        }
        return mmkv
    }
}
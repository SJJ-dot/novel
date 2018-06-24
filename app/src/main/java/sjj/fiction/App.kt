package sjj.fiction

import android.app.Application
import android.content.Intent
import android.os.StrictMode
import com.facebook.drawee.backends.pipeline.Fresco
import io.reactivex.plugins.RxJavaPlugins
import org.jetbrains.anko.newTask
import org.jetbrains.anko.noHistory
import sjj.alog.Config
import sjj.alog.Log
import java.util.*

/**
 * Created by SJJ on 2017/9/3.
 */
class App : Application() {
    companion object {
        lateinit var app: App
    }

    val activitys = LinkedList<BaseActivity>()
    override fun onCreate() {
        super.onCreate()
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            Log.e("UncaughtException",e)
            finishAll()
            val intent = Intent(this, CrashActivity::class.java)
            intent.putExtra(CrashActivity.THREAD_INFO, "线程：${t.name} ID：${t.id}")
            intent.putExtra(CrashActivity.CRASH_DATA, e)
            intent.noHistory()
            startActivity(intent)
            System.exit(0)
        }
        app = this
        val logConfig = Config()
        logConfig.hold = true
        logConfig.holdMultiple = false
        logConfig.holdLev = Config.ERROR
        Config.init(logConfig)

        Fresco.initialize(this);

        // 分别为MainThread和VM设置Strict Mode
        if (BuildConfig.DEBUG) {
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

    fun finishAll() {
        activitys.forEach { it.finish() }
        activitys.clear()
    }
}
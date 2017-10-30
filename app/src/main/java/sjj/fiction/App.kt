package sjj.fiction

import android.app.Application
import android.content.Intent
import com.facebook.drawee.backends.pipeline.Fresco
import com.raizlabs.android.dbflow.config.DatabaseConfig
import com.raizlabs.android.dbflow.config.FlowConfig
import com.raizlabs.android.dbflow.config.FlowManager
import org.jetbrains.anko.newTask
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

    lateinit var config: Configuration
    val activitys = LinkedList<BaseActivity>()
    override fun onCreate() {
        super.onCreate()
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            Log.e("UncaughtException",e)
            val intent = Intent(this, CrashActivity::class.java)
            intent.putExtra(CrashActivity.THREAD_INFO, "线程：${t.name} ID：${t.id}")
            intent.putExtra(CrashActivity.CRASH_DATA, e)
            intent.newTask()
            startActivity(intent)
            finishAll()
        }
        app = this
        config = Configuration(this)
        val logConfig = Config()
        logConfig.hold = true
        logConfig.holdMultiple = false
        logConfig.holdLev = Config.ERROR
        Config.init(logConfig)
        FlowManager.init(FlowConfig.builder(this)
                .addDatabaseConfig(DatabaseConfig.builder(BookDataBase::class.java).build())
                .build())
        Fresco.initialize(this);
    }

    fun exit() {
        System.exit(0)
    }

    fun finishAll() {
        activitys.forEach { it.finish() }
        activitys.clear()
    }
}
package sjj.fiction

import android.app.Application
import com.raizlabs.android.dbflow.config.DatabaseConfig
import com.raizlabs.android.dbflow.config.FlowConfig
import com.raizlabs.android.dbflow.config.FlowManager
import sjj.alog.Config
import sjj.alog.Log

/**
 * Created by SJJ on 2017/9/3.
 */
class App : Application() {
    companion object {
        lateinit var app: App
    }

    lateinit var config: Configuration
    override fun onCreate() {
        super.onCreate()
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            Log.e(t.name, e)
        }
        app = this
        config = Configuration(this)
        val logConfig = Config()
        logConfig.hold = true
        logConfig.holdMultiple = false
        logConfig.holdLev = Config.ERROR
        FlowManager.init(FlowConfig.builder(this)
                .addDatabaseConfig(DatabaseConfig.builder(BookDataBase::class.java).build())
                .build())
    }

    fun exit() {
        System.exit(0)
    }
}
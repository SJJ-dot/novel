package sjj.fiction

import android.app.Application
import sjj.alog.Config

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
        app = this
        config = Configuration(this)
        val logConfig = Config()
        logConfig.hold = true
        logConfig.holdMultiple = false
        logConfig.holdLev = Config.ERROR
    }

    fun exit() {
        System.exit(0)
    }
}
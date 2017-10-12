package sjj.fiction

import android.app.Application
import sjj.fiction.util.DataRepository

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
        DataRepository.initDataSource(this)
    }

    fun exit() {
        System.exit(0)
    }
}
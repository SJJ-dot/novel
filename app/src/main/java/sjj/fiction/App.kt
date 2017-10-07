package sjj.fiction

import android.app.Application
import sjj.fiction.util.SourcesUtil

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
        SourcesUtil.initDataSource(this)
    }
}
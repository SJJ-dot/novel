package sjj.fiction

import sjj.fiction.util.liveDataDelegate
import sjj.fiction.util.sharedPreferencesDelegate

object AppConfig {

    val ttf by liveDataDelegate("Roboto-Black.ttf")

    val readChapterTextSize by liveDataDelegate(24f)

    val searchHistory by liveDataDelegate<List<String>>(listOf())

}
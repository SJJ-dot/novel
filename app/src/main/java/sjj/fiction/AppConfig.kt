package sjj.fiction

import sjj.fiction.util.DelegateLiveData
import sjj.fiction.util.DelegateSharedPreferences

object AppConfig {

    val ttf by DelegateLiveData("Roboto-Black.ttf")

    val readChapterTextSize by DelegateLiveData(24f)

//    val searchHistory by DelegateLiveData<List<String>>(listOf())

    var offest by DelegateSharedPreferences(0)

    //已经迁移到到mmkv 的 SharedPreferences 文件记录
    var migratedSharedPreferences by DelegateSharedPreferences<MutableSet<String>>(mutableSetOf())

    /**
     * 被拒绝的权限 记录 用于在第一次被拒绝时提示用户
     */
    var deniedPermissions by DelegateSharedPreferences<MutableSet<String>>(mutableSetOf())


}

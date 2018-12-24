package sjj.novel

import sjj.novel.util.DelegateLiveData
import sjj.novel.util.DelegateSharedPreferences
import sjj.novel.util.toDpx
import sjj.novel.view.reader.page.PageMode
import sjj.novel.view.reader.page.PageStyle

object AppConfig {

    /**
     * 被拒绝的权限 记录 用于在第一次被拒绝时提示用户
     */
    var deniedPermissions by DelegateSharedPreferences<MutableSet<String>>(mutableSetOf())


    val gitHubAuthToken by DelegateLiveData("")

    /**
     * 默认的小说书源 域名
     */
    var defaultNovelSourceTLD by DelegateSharedPreferences(setOf<String>())

    var flipPageMode by DelegateSharedPreferences(PageMode.SIMULATION.name)


    /**
     * 亮度跟随系统
     */
    var isBrightnessFollowSys by DelegateSharedPreferences(true)
    /**
     * 亮度 0 - 1
     */
    var screenBrightnessProgress by DelegateSharedPreferences(1f)

    val readerPageStyle by DelegateLiveData(PageStyle.BG_def, toString = { it.name }, fromString = { PageStyle.valueOf(it!!) })
    /**
     * 阅读器文字大小
     */
    val fontSize by DelegateLiveData(28.toDpx())
}

package sjj.novel.util

import android.app.Activity
import android.provider.Settings
import sjj.alog.Log
import sjj.novel.AppConfig

fun initScreenBrightness(activity: Activity) {
    if (!AppConfig.isBrightnessFollowSys) {
        setScreenBrightness(activity,AppConfig.screenBrightnessProgress)
    }
}

/**
 * @param value 0-1
 */
fun setScreenBrightness(activity: Activity, value: Float) {
    activity.window?.also {
        val params = it.attributes
        params.screenBrightness = value
        it.attributes = params
    }
}

fun getScreenBrightness(activity: Activity): Int {
    return try {
        val cr = activity.contentResolver
        Settings.System.getInt(cr, Settings.System.SCREEN_BRIGHTNESS)
    } catch (e: Settings.SettingNotFoundException) {
        0
    }
}
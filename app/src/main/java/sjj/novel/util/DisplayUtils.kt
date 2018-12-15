package sjj.novel.util

import android.util.DisplayMetrics
import sjj.novel.Session


val displayMetrics: DisplayMetrics
    get() = Session.ctx.resources.displayMetrics

fun heightPixels(): Int {
    //当前可用的高度不包含状态栏 。全屏时候获取应该会包含状态栏 （可用的高度）
    return displayMetrics.heightPixels
}
package sjj.fiction.util

import sjj.fiction.App

/**
 * Created by SJJ on 2017/10/8.
 */
fun dp2Px(value: Int): Int {
    val scale = App.app.resources.displayMetrics.density
    return Math.round(value * scale).toInt()
}

fun sp2px(value: Int): Int {
    val fontScale = App.app.resources.displayMetrics.scaledDensity
    return Math.round(value * fontScale).toInt()
}

fun Int.toDpx() = dp2Px(this)
fun Int.toSpx() = sp2px(this)
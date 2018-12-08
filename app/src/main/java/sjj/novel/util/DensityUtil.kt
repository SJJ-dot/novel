package sjj.novel.util

import sjj.novel.Session

/**
 * Created by SJJ on 2017/10/8.
 */
fun dp2Px(value: Int): Int {
    val scale = Session.ctx.resources.displayMetrics.density
    return Math.round(value * scale)
}

fun sp2px(value: Int): Int {
    val fontScale = Session.ctx.resources.displayMetrics.scaledDensity
    return Math.round(value * fontScale)
}

fun Int.toDpx() = dp2Px(this)
fun Int.toSpx() = sp2px(this)
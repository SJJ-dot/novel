package sjj.novel.util

import android.view.ViewGroup

/**
 * Created by Administrator on 2017/10/13.
 */
inline fun <reified T : ViewGroup.LayoutParams, R : ViewGroup> R.lparams(
        width: Int = android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
        height: Int = android.view.ViewGroup.LayoutParams.WRAP_CONTENT
): R = lparams<T, R>(width, height) { }

inline fun <reified T : ViewGroup.LayoutParams, R : ViewGroup> R.lparams(
        width: Int = android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
        height: Int = android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
        init: T.() -> Unit
): R {
    val constructor = T::class.java.getConstructor(Int::class.java, Int::class.java)
    val param = constructor.newInstance(width, height)
    param.init()
    layoutParams = param
    return this
}
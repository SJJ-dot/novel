package sjj.novel.util

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import sjj.alog.Log
import java.lang.Exception
import java.net.URL

/**
 * Created by SJJ on 2017/10/15.
 */

val String.host: String
    get() = try {
        URL(this).host
    } catch (e: Exception) {
        "http://a"
    }

fun <T> T.log(): T {
    if (this is Throwable) {
        Log.e(1, this, this)
    } else {
        Log.e(1, this)
    }
    return this
}

fun Throwable.stackTraceString(): String {
    val buffer = StringBuilder()
    buffer.append(this::class.java).append(", ").append(message).append("\n")
    var throwable: Throwable? = this
    while (throwable != null) {
        for (element in throwable.stackTrace) {
            buffer.append(element.toString()).append("\n")
        }
        throwable = throwable.cause
        if (throwable != null)
            buffer.append("caused by ")
    }
    return buffer.toString()
}

inline fun <reified T : androidx.fragment.app.Fragment> androidx.fragment.app.FragmentActivity.getFragment(containerViewId: Int = 0, tag: String): T {
    val byTag = supportFragmentManager.findFragmentByTag(tag) ?: T::class.java.newInstance().also {
        supportFragmentManager.beginTransaction().add(containerViewId, it, tag).commit()
    }
    return byTag as T

}
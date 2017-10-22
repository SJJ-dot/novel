package sjj.fiction.util


import android.os.SystemClock
import android.view.View
import sjj.fiction.R
import java.util.Arrays

/**
 * Created by SJJ on 2017/10/22.
 */
fun isFast(view: View, time: Long = 1000): Boolean {
    val tag = view.getTag(R.id.click_tag)
    val timeMillis = System.currentTimeMillis()
    view.setTag(R.id.click_tag, timeMillis)
    if (tag != null) {
        val lastTime = tag as Long
        return timeMillis - lastTime < time
    }
    return false
}

fun clickCount(view: View, count: Int, timeLimit: Int = count * 250): Boolean {
    var tag = view.getTag(R.id.click_count_tag) as LongArray?
    if (tag == null) {
        tag = LongArray(count)
        view.setTag(R.id.click_count_tag, tag)
    }
    System.arraycopy(tag, 1, tag, 0, tag.size - 1)
    tag[tag.size - 1] = SystemClock.uptimeMillis()
    if (tag[0] >= SystemClock.uptimeMillis() - timeLimit) {
        Arrays.fill(tag, 0)
        return true
    }
    return false
}

fun isDoubleClick(view: View): Boolean {
    return clickCount(view, 2)
}
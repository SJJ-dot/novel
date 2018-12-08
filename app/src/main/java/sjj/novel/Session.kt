package sjj.novel

import android.annotation.SuppressLint
import android.content.Context
import java.util.*

@SuppressLint("StaticFieldLeak")
object Session {
    lateinit var ctx: Context
    val activitys = LinkedList<BaseActivity>()

    fun finishAllActivity() {
        activitys.forEach { it.finish() }
        activitys.clear()
    }
}
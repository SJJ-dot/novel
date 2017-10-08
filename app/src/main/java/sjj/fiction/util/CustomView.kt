package sjj.fiction.util

import android.content.Context
import android.support.v4.widget.TextViewCompat
import android.support.v7.widget.AppCompatTextView
import android.view.ViewManager
import android.widget.TextView
import org.jetbrains.anko.custom.ankoView

/**
 * Created by SJJ on 2017/10/8.
 */
inline fun ViewManager.textView() = textView {}

inline fun ViewManager.textView(init: TextView.() -> Unit): TextView {
    return ankoView({ AppCompatTextView(it) }, theme = 0, init = init)
}
inline fun Context.textView() = textView {}

inline fun Context.textView(init: TextView.() -> Unit): TextView {
    return ankoView({ AppCompatTextView(it) }, theme = 0, init = init)
}
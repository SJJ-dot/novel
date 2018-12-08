package sjj.novel.util

import android.content.Context
import android.support.v7.widget.AppCompatTextView
import android.support.v7.widget.CardView
import android.view.ViewManager
import android.widget.TextView
import org.jetbrains.anko.custom.ankoView

/**
 * Created by sjj on 2017/10/13.
 */
inline fun ViewManager.textView() = textView {}

inline fun ViewManager.textView(init: TextView.() -> Unit): TextView {
    return ankoView({ AppCompatTextView(it) }, theme = 0, init = init)
}

inline fun Context.textView() = textView {}

inline fun Context.textView(init: TextView.() -> Unit): TextView {
    return ankoView({ AppCompatTextView(it) }, theme = 0, init = init)
}

inline fun ViewManager.cardView() = cardView {}

inline fun ViewManager.cardView(init: CardView.() -> Unit): CardView {
    return ankoView({ CardView(it) }, theme = 0, init = init)
}

inline fun Context.cardView() = cardView {}

inline fun Context.cardView(init: _CardView.() -> Unit): CardView {
    return ankoView({ _CardView(it) }, theme = 0, init = init)
}
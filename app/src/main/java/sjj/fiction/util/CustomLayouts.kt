package sjj.fiction.util

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout

/**
 * Created by Administrator on 2017/10/13.
 */
class _CardView(context: Context) : CardView(context) {
    inline fun <T : View> T.lparams(
            width: Int = android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
            height: Int = android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
            init: FrameLayout.LayoutParams.() -> Unit
    ): T {
        val layoutParams = FrameLayout.LayoutParams(width, height)
        layoutParams.init()
        this@lparams.layoutParams = layoutParams
        return this
    }
}

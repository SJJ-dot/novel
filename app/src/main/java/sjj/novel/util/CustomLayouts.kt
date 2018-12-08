package sjj.novel.util

import android.content.Context
import android.support.v7.widget.CardView
import android.view.View
import android.widget.FrameLayout

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

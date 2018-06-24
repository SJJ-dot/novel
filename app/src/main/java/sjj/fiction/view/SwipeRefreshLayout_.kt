package sjj.fiction.view

import android.content.Context
import android.support.v4.widget.SwipeRefreshLayout
import android.util.AttributeSet
import android.view.MotionEvent
import android.support.v4.view.ViewCompat
import android.view.ViewGroup
import android.R.attr.y
import android.R.attr.x
import android.graphics.Rect
import android.view.View


class SwipeRefreshLayout_ @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : SwipeRefreshLayout(context, attrs) {
    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (canChildScrollUp(this,ev.x.toInt(),ev.y.toInt())) {
            return false
        }
        return super.onInterceptTouchEvent(ev)
    }


    private fun canChildScrollUp(view: View, x: Int, y: Int): Boolean {
        if (!inRangeOfView(view, x, y)) return false

        if (view is ViewGroup) {
            val count = view.childCount
            for (i in 0 until count) {
                val child = view.getChildAt(i)
                if (canChildScrollUp(child, x, y))
                    return true
            }
        }
        return view.canScrollVertically( -1)
    }

    private val mTouchFrame = Rect()
    private fun inRangeOfView(view: View, x: Int, y: Int): Boolean {
        val frame = mTouchFrame
        if (view.visibility == View.VISIBLE) {
            view.getHitRect(frame)
            return frame.contains(x, y)
        }
        return false
    }
}
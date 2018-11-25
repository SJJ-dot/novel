package sjj.fiction.view

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration

class MyRecycleView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    override fun computeScroll() {
        super.computeScroll()
    }

    override fun onTouchEvent(e: MotionEvent?): Boolean {
        return super.onTouchEvent(e)
    }
}
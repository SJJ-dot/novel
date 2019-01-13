package sjj.novel.reader.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.View.MeasureSpec.*
import androidx.appcompat.widget.AppCompatTextView

class PageTextView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(makeMeasureSpec(getSize(widthMeasureSpec), AT_MOST),
                makeMeasureSpec(getSize(heightMeasureSpec), AT_MOST))
    }
}
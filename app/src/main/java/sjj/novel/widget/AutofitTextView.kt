package sjj.novel.widget

import android.content.Context
import androidx.appcompat.widget.AppCompatTextView
import android.util.AttributeSet
import android.util.TypedValue


class AutofitTextView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {
    //    private var paint: TextPaint? = null
    private var minTextSize: Float = 0.toFloat()
    private var maxTextSize: Float = 0.toFloat()
    private val DEFAULT_MIN_TEXT_SIZE = 8f
    init {
        //        paint = getPaint()
        maxTextSize = textSize
        // if (maxTextSize >= DEFAULT_MIN_TEXT_SIZE) {
        // maxTextSize = DEFAULT_MAX_TEXT_SIZE;
        // }
        minTextSize = DEFAULT_MIN_TEXT_SIZE
        setSingleLine(true)
        ellipsize = null
        setHorizontallyScrolling(false)
        // setGravity(Gravity.CENTER);
        // //如果不调用ondraw，另外画text的话当setSingleLine（true）和setGravity（Gravity。center）同时设置时textView中的内容无法正常显示
    }

    /**
     * Re size the font so the specified text fits in the text box * assuming
     * the text box is the specified width.
     */
    /**
     * 根据文字内容和textView的大小设置文字大小使文字能全部显示
     *
     * @param text
     * textView中的内容
     * @param textWidth
     * textView控件的宽度
     * @param textHeight
     * textView控件的高度
     */
    private fun refitText(text: String, textWidth: Int, textHeight: Int) {
        if (textWidth > 0) {
            // 得到textView中显示文本部分的长度
            val availableWidth = textWidth - paddingLeft - paddingRight
            var maxSize = maxTextSize
            var minSize = minTextSize
            var mid = maxSize
            val widths = FloatArray(text.length)
            setTextSize(TypedValue.COMPLEX_UNIT_PX, mid)
            var widthSum = getTextWidth(text, widths)
            if (widthSum < availableWidth) {
                requestLayout()
                invalidate()
                return
            }
            while (true) {
                if (maxSize - minSize < 0.01) {
                    requestLayout()
                    invalidate()
                }
                if (widthSum <= availableWidth) {
                    if (Math.abs(maxSize - minSize) < 0.5f) {
                        requestLayout()
                        invalidate()
                        return
                    }
                    minSize = mid
                    mid = (minSize + maxSize) / 2
                } else {
                    maxSize = mid
                    mid = (minSize + maxSize) / 2
                }
                setTextSize(TypedValue.COMPLEX_UNIT_PX, mid)
                widthSum = getTextWidth(text, widths)
            }
        }
    }

    private fun getTextWidth(text: String, widths: FloatArray): Float {
        paint!!.getTextWidths(text, widths)
        var widthSum = 0f
        for (f in widths) {
            widthSum += f
        }
        return widthSum
    }

    override fun onTextChanged(text: CharSequence, start: Int, before: Int, after: Int) {
        refitText(text.toString(), width, height)
        super.onTextChanged(text, start, before, after)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        if (w != oldw) {
            refitText(text.toString(), w, h)
        }
        super.onSizeChanged(w, h, oldw, oldh)
    }

}
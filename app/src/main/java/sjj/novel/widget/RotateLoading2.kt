package sjj.novel.widget

import android.content.Context
import android.util.AttributeSet
import com.victor.loading.rotate.RotateLoading
import sjj.alog.Log

class RotateLoading2 @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RotateLoading(context, attrs, defStyleAttr) {
    fun setLoading(boolean: Boolean) {
        if (boolean && !isStart) {
            start()
        } else if (!boolean && isStart) {
            stop()
        }
    }
}
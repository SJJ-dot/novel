package sjj.novel.widget

import android.content.Context
import android.util.AttributeSet
import androidx.drawerlayout.widget.DrawerLayout
import sjj.alog.Log
import sjj.novel.util.log
import java.lang.Exception

class DrawerLayoutTest @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : DrawerLayout(context, attrs, defStyleAttr) {
    override fun setTag(tag: Any?) {
        super.setTag(tag)
        Log.e("setTag",Exception())
    }
}
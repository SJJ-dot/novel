package sjj.novel.reader.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import sjj.novel.reader.model.TextPage

class ReaderView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {
    override fun onTouchEvent(e: MotionEvent?): Boolean {
        return super.onTouchEvent(e)
    }
    class PageAdapter : Adapter<ViewHolder>() {
        val pageList = mutableListOf<TextPage>()
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return object : ViewHolder(PageTextView(parent.context)) {}
        }

        override fun getItemCount(): Int {
            return pageList.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }
}
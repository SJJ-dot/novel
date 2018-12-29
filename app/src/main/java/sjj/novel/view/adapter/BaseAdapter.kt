package sjj.novel.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

abstract class BaseAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    @Deprecated("use itemLayoutRes", ReplaceWith("itemLayoutRes"))
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        return createViewHolder(LayoutInflater.from(parent.context).inflate(itemLayoutRes(viewType), parent, false))
    }

    protected fun createViewHolder(view: View): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        return object : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        }
    }


    protected open fun itemLayoutRes(viewType: Int): Int {
        return 0
    }

}
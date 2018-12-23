package sjj.novel.view

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import sjj.novel.R

abstract class BaseAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
        return createViewHolder(LayoutInflater.from(p0.context).inflate(itemLayoutRes(p1), p0, false))
    }

    protected fun createViewHolder(view: View): RecyclerView.ViewHolder {
        return object : RecyclerView.ViewHolder(view) {
        }
    }


    protected open fun itemLayoutRes(pos: Int): Int {
        return 0
    }

}
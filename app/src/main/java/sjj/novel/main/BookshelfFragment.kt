package sjj.novel.main

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_books.*
import kotlinx.android.synthetic.main.item_book_list.view.*
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.toast
import sjj.alog.Log
import sjj.novel.BaseFragment
import sjj.novel.DISPOSABLE_ACTIVITY_MAIN_REFRESH
import sjj.novel.R
import sjj.novel.details.DetailsActivity
import sjj.novel.model.Book
import sjj.novel.util.getModel
import sjj.novel.util.host

/**
 * Created by SJJ on 2017/10/7.
 */
class BookshelfFragment : BaseFragment() {

    private val model by lazy { getModel<MainViewModel>() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_books, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bookList.layoutManager = LinearLayoutManager(context)
        val adapter = Adapter()
        bookList.adapter = adapter
        model.books.observeOn(AndroidSchedulers.mainThread()).subscribe {
            adapter.data = it
            adapter.notifyDataSetChanged()
        }.destroy()
        bookListRefreshLayout.setOnRefreshListener {
            model.refresh()
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnError {
                        toast("$it")
                    }.doOnTerminate {
                        bookListRefreshLayout.isRefreshing = false
                    }.subscribe()
                    .destroy(DISPOSABLE_ACTIVITY_MAIN_REFRESH)
        }
    }

    private inner class Adapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var data: List<Book>? = null
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return object : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_book_list, parent, false)) {}
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val book = data!![position]
            holder.itemView.bookName.text = book.name
            holder.itemView.author.text = book.author
            holder.itemView.originWebsite.text = book.url.host
            holder.itemView.lastChapter.text = book.chapterList.lastOrNull()?.chapterName
            holder.itemView.bookCover.setImageURI(book.bookCoverImgUrl)
            holder.itemView.setOnClickListener { v ->
                startActivity<DetailsActivity>(DetailsActivity.BOOK_NAME to book.name, DetailsActivity.BOOK_AUTHOR to book.author)
            }
            holder.itemView.setOnLongClickListener {
                alert {
                    title = "确认删除？"
                    message = "确认删除书籍：${book.name}？"
                    negativeButton("取消") {}
                    positiveButton("删除") {
                        model.delete(book)
                    }
                }.show()
                true
            }

            if (book.loadStatus == Book.LoadState.Loading) {
                holder.itemView.bv_unread.visibility = View.INVISIBLE
                holder.itemView.rl_loading.visibility = View.VISIBLE
                holder.itemView.rl_loading.start()
            } else {
                holder.itemView.bv_unread.visibility = View.VISIBLE
                holder.itemView.bv_unread.badgeCount = maxOf((book.chapterList.lastOrNull()?.index?:0) - book.index,0)
                holder.itemView.rl_loading.visibility = View.INVISIBLE
                holder.itemView.rl_loading.stop()
            }
        }

        override fun getItemCount(): Int = data?.size ?: 0
    }
}
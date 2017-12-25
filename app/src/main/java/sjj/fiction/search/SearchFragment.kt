package sjj.fiction.search

import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.support.v7.widget.CardView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_search.*
import org.jetbrains.anko.find
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.support.v4.indeterminateProgressDialog
import org.jetbrains.anko.support.v4.progressDialog
import org.jetbrains.anko.support.v4.toast
import sjj.alog.Log
import sjj.fiction.BaseFragment
import sjj.fiction.R
import sjj.fiction.model.BookGroup
import sjj.fiction.util.*

/**
 * Created by SJJ on 2017/10/7.
 */
class SearchFragment : BaseFragment(), SearchContract.view {
    private lateinit var presenter: SearchContract.presenter
    private val searchResultBookAdapter by lazy { SearchResultBookAdapter() }
    private var progressDialog: ProgressDialog? = null

    init {
        SearchPresenter(this)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        searchRecyclerView.layoutManager = LinearLayoutManager(context)
        searchRecyclerView.adapter = searchResultBookAdapter
        presenter.start()
    }

    override fun onStop() {
        super.onStop()
        presenter.stop()
    }

    fun showBookList(book: List<BookGroup>) {
        var adapter = searchRecyclerView?.adapter ?: return
        adapter = if (adapter !is SearchResultBookAdapter) searchResultBookAdapter else adapter
        searchRecyclerView.adapter = adapter
        adapter.data = book
        adapter.notifyDataSetChanged()
    }

    override fun setPresenter(presenter: SearchContract.presenter) {
        this.presenter = presenter
    }

    override fun setLoadBookDetailsErrorHint(it: Throwable) {
        toast("加载书籍详情出错：${it.message}")
        Log.e("",it)
    }

    override fun setLoadBookDetailsHint(active: Boolean) {
        progressDialog = if (active) {
            progressDialog ?: indeterminateProgressDialog("正在加载书籍请稍候……")
        } else {
            progressDialog?.dismiss()
            null
        }
    }

    private inner class SearchResultBookAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var data = listOf<BookGroup>()
        override fun getItemCount(): Int = data.size
        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
            return object : RecyclerView.ViewHolder(with(parent!!.context) {
                cardView {
                    linearLayout {
                        textView {
                            id = R.id.searchItemTitle
                            setPadding(16.toDpx(), 5.toDpx(), 16.toDpx(), 5.toDpx())
                            textSize = 16f
                        }.lparams {
                            weight = 1.0f
                        }
                        textView {
                            id = R.id.searchItemAuthor
                            setPadding(16.toDpx(), 5.toDpx(), 16.toDpx(), 5.toDpx())
                            textSize = 16f
                        }
                        textView {
                            id = R.id.searchItemOrigin
                            setPadding(16.toDpx(), 5.toDpx(), 16.toDpx(), 5.toDpx())
                            textSize = 16f
                        }
                    }.lparams {
                        width = ViewGroup.LayoutParams.MATCH_PARENT
                        leftMargin = 16.toDpx()
                        topMargin = 5.toDpx()
                        rightMargin = 16.toDpx()
                        bottomMargin = 5.toDpx()
                    }
                }.lparams<RecyclerView.LayoutParams, CardView>(width = -1) {
                    leftMargin = 16.toDpx()
                    topMargin = 5.toDpx()
                    rightMargin = 16.toDpx()
                    bottomMargin = 5.toDpx()
                }
            }) {}
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val bookGroup = data[position]
            val book = bookGroup.currentBook
            holder.itemView.find<TextView>(R.id.searchItemTitle).text = book.name
            holder.itemView.find<TextView>(R.id.searchItemAuthor).text = book.author
            holder.itemView.find<TextView>(R.id.searchItemOrigin).text = bookGroup.books.size.toString()
            holder.itemView.setOnClickListener { v ->
                presenter.onSelect(bookGroup, context)
            }
        }

    }
}
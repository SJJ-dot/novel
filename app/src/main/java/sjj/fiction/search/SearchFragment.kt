package sjj.fiction.search

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.CardView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_search.*
import org.jetbrains.anko.find
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.indeterminateProgressDialog
import sjj.alog.Log
import sjj.fiction.BaseFragment
import sjj.fiction.R
import sjj.fiction.details.DetailsActivity
import sjj.fiction.model.Book
import sjj.fiction.model.BookGroup
import sjj.fiction.util.cardView
import sjj.fiction.util.lparams
import sjj.fiction.util.textView
import sjj.fiction.util.toDpx

/**
 * Created by SJJ on 2017/10/7.
 */
class SearchFragment : BaseFragment(), SearchContract.view {
    private val presenter = SearchPresenter(this)
    private val searchResultBookAdapter by lazy { SearchResultBookAdapter() }
    private var compDisposable: CompositeDisposable = CompositeDisposable()
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        searchRecyclerView.layoutManager = LinearLayoutManager(context)
        searchRecyclerView.adapter = searchResultBookAdapter
    }

    override fun onStart() {
        super.onStart()
        presenter.start()
    }

    override fun onStop() {
        super.onStop()
        presenter.stop()
        compDisposable.dispose()
        compDisposable = CompositeDisposable()
    }

    fun search(text: String) {
        val dialog = indeterminateProgressDialog("请稍候")
        compDisposable.add(presenter.search(if (text.isNotEmpty()) text else "极道天魔").subscribe({
            dialog.dismiss()
            showBookList(it)
        }, {
            dialog.dismiss()
            Log.e("error", it)
        }))
    }

    override fun setPresenter(presenter: SearchContract.presenter) {
    }

    override fun showBookList(book: List<BookGroup>) {
        var adapter = searchRecyclerView.adapter;
        adapter = if (adapter !is SearchResultBookAdapter) searchResultBookAdapter else adapter
        searchRecyclerView.adapter = adapter
        adapter.data = book
        adapter.notifyDataSetChanged()
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
                if (bookGroup.books.size > 1) {
                    alert {
                        items(bookGroup.books.map { it.url.domain().url }) { d, i ->
                            d.dismiss()
                            bookGroup.currentBook = bookGroup.books[i]
                            startActivity(v.context, bookGroup)
                        }
                    }.show()
                } else {
                    startActivity(v.context, bookGroup)
                }

            }
        }

        fun startActivity(context: Context, book: BookGroup) {
            val dialog = indeterminateProgressDialog("请稍候")
            compDisposable.add(presenter.onSelect(book).subscribe({
                dialog.dismiss()
                val intent = Intent(context, DetailsActivity::class.java);
                intent.putExtra(DetailsActivity.data, it)
                startActivity(intent)
            }, {
                dialog.dismiss()
                Log.e("error", it)
            }))
        }
    }

}
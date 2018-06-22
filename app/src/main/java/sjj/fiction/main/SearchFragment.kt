package sjj.fiction.main

import android.app.ProgressDialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.CardView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_search.*
import org.jetbrains.anko.find
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.support.v4.indeterminateProgressDialog
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.toast
import sjj.alog.Log
import sjj.fiction.AppConfig
import sjj.fiction.AppConfig.searchHistory
import sjj.fiction.BaseFragment
import sjj.fiction.R
import sjj.fiction.details.DetailsActivity
import sjj.fiction.model.Book
import sjj.fiction.model.BookSourceRecord
import sjj.fiction.util.cardView
import sjj.fiction.util.lparams
import sjj.fiction.util.textView
import sjj.fiction.util.toDpx

/**
 * Created by SJJ on 2017/10/7.
 */
class SearchFragment : BaseFragment() {
    private val model by lazy { ViewModelProviders.of(this).get(MainViewModel::class.java) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        searchRecyclerView.layoutManager = LinearLayoutManager(context)
        val resultBookAdapter = SearchResultBookAdapter()
        searchRecyclerView.adapter = resultBookAdapter
        searchCancel.setOnClickListener {
            searchInput.setText("")
        }
        val adapter = ArrayAdapter<String>(context, R.layout.item_text_text, R.id.text1)
        searchInput.setAdapter(adapter)
        searchHistory.observe(this, Observer {
            adapter.clear()
            adapter.addAll(it)
            adapter.notifyDataSetChanged()
        })
        searchInput.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                model.search(searchInput.text.toString()).subscribe({ ls ->
                    resultBookAdapter.data = ls
                    resultBookAdapter.notifyDataSetChanged()
                }, {
                    toast("$it")
                }).destroy("searchBook")
                return@OnEditorActionListener true
            }
            false
        })
    }

    private inner class SearchResultBookAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var data = listOf<Pair<BookSourceRecord, List<Book>>>()
        override fun getItemCount(): Int = data.size
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return object : RecyclerView.ViewHolder(with(parent.context) {
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
            val book = bookGroup.second.first()
            holder.itemView.find<TextView>(R.id.searchItemTitle).text = book.name
            holder.itemView.find<TextView>(R.id.searchItemAuthor).text = book.author
            holder.itemView.find<TextView>(R.id.searchItemOrigin).text = bookGroup.second.size.toString()
            holder.itemView.setOnClickListener { v ->
                model.saveBookSourceRecord(data).subscribe { _ ->
                    startActivity<DetailsActivity>(DetailsActivity.book_url to book.url)
                }
            }
        }

    }
}
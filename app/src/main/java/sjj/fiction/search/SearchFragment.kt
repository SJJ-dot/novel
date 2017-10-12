package sjj.fiction.search

import android.content.Intent
import android.os.Bundle
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
import org.jetbrains.anko.support.v4.indeterminateProgressDialog
import org.jetbrains.anko.verticalLayout
import sjj.alog.Log
import sjj.fiction.BaseFragment
import sjj.fiction.R
import sjj.fiction.data.Repository.FictionDataRepository
import sjj.fiction.details.DetailsActivity
import sjj.fiction.model.Book
import sjj.fiction.model.SearchResultBook
import sjj.fiction.util.DATA_REPOSITORY_FICTION
import sjj.fiction.util.DataRepository
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
        val split = text.split(":")
        if (split.size > 1) {
            try {
                presenter.setSource(split[1].toInt())
            } catch (e: Exception) {
            }
        }
        val dialog = indeterminateProgressDialog("请稍候")
        compDisposable.add(presenter.search(if (split[0].isNotEmpty()) split[0] else "极道天魔").subscribe({
            dialog.dismiss()
            Log.e(it)
            showBookList(it)
        }, {
            dialog.dismiss()
            Log.e("error", it)
        }))
    }

    override fun setPresenter(presenter: SearchContract.presenter) {
    }

    override fun showBookList(book: List<SearchResultBook>) {
        var adapter = searchRecyclerView.adapter;
        adapter = if (adapter !is SearchResultBookAdapter) searchResultBookAdapter else adapter
        searchRecyclerView.adapter = adapter
        adapter.data = book
        adapter.notifyDataSetChanged()
    }

    override fun showBookUrls(book: Book) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private inner class SearchResultBookAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var data = listOf<SearchResultBook>()
        override fun getItemCount(): Int = data.size
        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
            return object : RecyclerView.ViewHolder(with(parent!!.context) {
                linearLayout {
                    textView {
                        id = R.id.searchItemTitle
                        setPadding(16.toDpx(), 5.toDpx(), 16.toDpx(), 5.toDpx())
                        textSize = 16f
                    }
                    textView {
                        id = R.id.searchItemAuthor
                        setPadding(16.toDpx(), 5.toDpx(), 16.toDpx(), 5.toDpx())
                        textSize = 16f
                    }
                }
            }) {}
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            holder.itemView.find<TextView>(R.id.searchItemTitle).text = data[position].name
            holder.itemView.find<TextView>(R.id.searchItemAuthor).text = data[position].author
            holder.itemView.setOnClickListener { v ->
                val dialog = indeterminateProgressDialog("请稍候")
                compDisposable.add(presenter.onSelect(data[position]).subscribe({
                    dialog.dismiss()
                    val intent = Intent(v.context, DetailsActivity::class.java);
                    intent.putExtra(DetailsActivity.data, it)
                    startActivity(intent)
                }, {
                    dialog.dismiss()
                    Log.e("error", it)
                }))
            }
        }
    }

}
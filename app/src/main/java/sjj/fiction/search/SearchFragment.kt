package sjj.fiction.search

import android.content.Context
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_search.*
import org.jetbrains.anko.coroutines.experimental.asReference
import org.jetbrains.anko.sdk25.coroutines.onClick
import sjj.alog.Log
import sjj.fiction.BaseFragment
import sjj.fiction.R
import sjj.fiction.data.Repository.SoduDataRepository
import sjj.fiction.model.Book
import sjj.fiction.model.SearchResultBook
import sjj.fiction.util.DATA_REPOSITORY_SODU
import sjj.fiction.util.DataRepository
import sjj.fiction.util.textView
import sjj.fiction.util.toDpx

/**
 * Created by SJJ on 2017/10/7.
 */
class SearchFragment : BaseFragment(), SearchContract.view {
    private val presenter = SearchPresenter(this)
    private val searchResultBookAdapter by lazy { SearchResultBookAdapter() }
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
    }

    fun search(text: String) {
        val data: SoduDataRepository = DataRepository[DATA_REPOSITORY_SODU]
        data.search(if (text.isNotEmpty()) text else "极道天魔").subscribe({ showBookList(it) }, { Log.e("error", it) })
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

    private class SearchResultBookAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var data = listOf<SearchResultBook>()
        override fun getItemCount(): Int = data.size
        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
            return object : RecyclerView.ViewHolder(with(parent!!.context) {
                textView {
                    setPadding(16.toDpx(), 5.toDpx(), 16.toDpx(), 5.toDpx())
                }
            }) {}
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val textView = holder.itemView as TextView
            textView.text = data[position].name
            textView.setOnClickListener {
                Log.e(data[position])
            }
        }
    }

}
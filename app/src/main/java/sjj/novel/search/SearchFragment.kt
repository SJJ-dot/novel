package sjj.novel.search

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.navigation.fragment.NavHostFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_search.*
import org.jetbrains.anko.appcompat.v7.coroutines.onClose
import org.jetbrains.anko.support.v4.longToast
import org.jetbrains.anko.support.v4.startActivity
import sjj.alog.Log
import sjj.novel.BaseFragment
import sjj.novel.R
import sjj.novel.databinding.ItemBookSearchListBinding
import sjj.novel.details.DetailsActivity
import sjj.novel.model.SearchHistory
import sjj.novel.util.lazyModel

/**
 * Created by SJJ on 2017/10/7.
 */
class SearchFragment : BaseFragment() {
    private val model by lazyModel<SearchViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.fragment_main_search_menu, menu)
        val searchView = menu?.findItem(R.id.search_view)?.actionView as SearchView
        searchView.queryHint = "请输入书名或者作者"
        searchView.imeOptions = EditorInfo.IME_ACTION_SEARCH
        init(searchView)
        searchView.isIconified = false
    }

    private fun init(searchView: SearchView) {
        searchRecyclerView.layoutManager = LinearLayoutManager(context)
        val resultBookAdapter = SearchResultBookAdapter()
        searchRecyclerView.adapter = resultBookAdapter
        searchView.onClose {
            NavHostFragment.findNavController(this@SearchFragment).navigateUp()
        }
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                if (p0.isNullOrEmpty()) return true
                model.addSearchHistory(SearchHistory(content = p0)).subscribe()
                searchView.clearFocus()
                refresh_progress_bar.isAutoLoading = true
                model.search(p0).observeOn(AndroidSchedulers.mainThread()).doAfterTerminate {
                    refresh_progress_bar.isAutoLoading = false
                }.subscribe({ ls ->
                    resultBookAdapter.data = ls
                    resultBookAdapter.notifyDataSetChanged()
                }, {
                    longToast("${it.message}")
                }).destroy("searchBook")
                return true
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                return false
            }
        })
        searchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                ll_search_history?.visibility = View.VISIBLE
                searchRecyclerView?.visibility = View.INVISIBLE
            } else {
                searchRecyclerView?.visibility = View.VISIBLE
                ll_search_history?.visibility = View.INVISIBLE
            }
        }
        model.getSearchHistory().observeOn(AndroidSchedulers.mainThread()).subscribe { history ->
            tfl_search_history.removeAllViews()
            history.forEach {
                val tagView = layoutInflater.inflate(R.layout.item_search_history, tfl_search_history, false) as TextView
                tfl_search_history.addView(tagView)
                tagView.text = it.content
                tagView.setOnClickListener { _ ->
                    searchView.setQuery(it.content, true)
                }
                tagView.setOnLongClickListener { _ ->
                    model.deleteSearchHistory(listOf(it)).subscribe()
                    true
                }
            }
            tv_search_history_clean.setOnClickListener {
                model.deleteSearchHistory(history).subscribe()
            }
        }.destroy("get Search History")
    }


    private inner class SearchResultBookAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var data = listOf<SearchViewModel.BookSearchItemViewModel>()
        override fun getItemCount(): Int = data.size
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val binding = DataBindingUtil.inflate<ItemBookSearchListBinding>(LayoutInflater.from(parent.context), R.layout.item_book_search_list, parent, false)
            return object : RecyclerView.ViewHolder(binding.root) {}
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val bind = DataBindingUtil.bind<ItemBookSearchListBinding>(holder.itemView)
            val bookGroup = data[position]
            bind?.model = bookGroup
            holder.itemView.setOnClickListener { _ ->
                model.saveBookSourceRecord(bookGroup.book).observeOn(AndroidSchedulers.mainThread()).subscribe { _ ->
                    startActivity<DetailsActivity>(DetailsActivity.BOOK_NAME to bookGroup.book.bookName, DetailsActivity.BOOK_AUTHOR to bookGroup.book.author)
                }
            }
        }

    }
}
package sjj.novel.main

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment.findNavController
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_books.*
import kotlinx.android.synthetic.main.item_book_list.view.*
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.toast
import sjj.novel.BaseFragment
import sjj.novel.DISPOSABLE_ACTIVITY_MAIN_REFRESH
import sjj.novel.R
import sjj.novel.databinding.ItemBookListBinding
import sjj.novel.details.DetailsActivity
import sjj.novel.model.Book
import sjj.novel.util.lazyModel

/**
 * Created by SJJ on 2017/10/7.
 */
class BookshelfFragment : BaseFragment() {

    private val model by lazyModel<BookShelfViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_books, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar.inflateMenu(R.menu.fragment_book_shelf)
        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.search_book_shelf -> {
                    findNavController(this).navigate(R.id.searchFragment)
                    true
                }
                else -> false
            }
        }

        bookList.layoutManager = LinearLayoutManager(context)
        val adapter = Adapter()
        adapter.setHasStableIds(true)
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
        var data: List<BookShelfViewModel.BookShelfItemViewModel>? = null
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val binding = DataBindingUtil.inflate<ItemBookListBinding>(LayoutInflater.from(parent.context), R.layout.item_book_list, parent, false)
            return object : RecyclerView.ViewHolder(binding.root) {}
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val bind = DataBindingUtil.bind<ItemBookListBinding>(holder.itemView)
            val viewModel = data!!.get(position)
            bind!!.model = viewModel
            holder.itemView.setOnClickListener { v ->
                startActivity<DetailsActivity>(DetailsActivity.BOOK_NAME to viewModel.book.name, DetailsActivity.BOOK_AUTHOR to viewModel.book.author)
            }
            holder.itemView.setOnLongClickListener { _ ->
                alert {
                    title = "确认删除？"
                    message = "确认删除书籍：${viewModel.bookName.get()}？"
                    negativeButton("取消") {}
                    positiveButton("删除") {
                        model.delete(viewModel.book)
                    }
                }.show()
                true
            }

            if (viewModel.book.loadStatus == Book.LoadState.Loading) {
                holder.itemView.bv_unread.visibility = View.INVISIBLE
                holder.itemView.rl_loading.visibility = View.VISIBLE
                holder.itemView.rl_loading.start()
            } else {
                holder.itemView.bv_unread.visibility = View.VISIBLE
                holder.itemView.rl_loading.visibility = View.INVISIBLE
                holder.itemView.rl_loading.stop()
            }
        }

        override fun getItemCount(): Int = data?.size ?: 0

        override fun getItemId(position: Int): Long {
            val viewModel = data?.get(position) ?: return 0
            return viewModel.book.id()
        }
    }
}
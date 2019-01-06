package sjj.novel.view.module.main

import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_IDLE
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_books.*
import kotlinx.android.synthetic.main.item_book_list.view.*
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.toast
import sjj.novel.BaseFragment
import sjj.novel.DISPOSABLE_ACTIVITY_MAIN_REFRESH
import sjj.novel.R
import sjj.novel.databinding.ItemBookListBinding
import sjj.novel.util.getModel
import sjj.novel.util.observeOnMain
import sjj.novel.util.requestOptions
import sjj.novel.view.adapter.BaseAdapter
import sjj.novel.view.fragment.ChooseBookSourceFragment
import sjj.novel.view.module.details.DetailsActivity
import sjj.novel.view.module.read.ReadActivity


/**
 * Created by SJJ on 2017/10/7.
 */
class BookshelfFragment : BaseFragment() {

    private lateinit var model: BookShelfViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_books, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        model = getModel()
        bookList.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        val adapter = Adapter()
        adapter.setHasStableIds(true)
        bookList.adapter = adapter

        val mItemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.Callback() {
            override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
                return makeMovementFlags(ItemTouchHelper.DOWN or ItemTouchHelper.UP,ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT)
            }

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                val removeAt = adapter.data.removeAt(viewHolder.adapterPosition)
                adapter.data.add(target.adapterPosition,removeAt)
                adapter.notifyItemMoved(viewHolder.adapterPosition,target.adapterPosition)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                adapter.data.getOrNull(viewHolder.adapterPosition)?.also {
                    model.delete(it.book)
                }
            }

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)
                if (actionState == ACTION_STATE_IDLE) {
                    val data = adapter.data.map { it.bookSourceRecord }
                    data.forEachIndexed { index, bookSourceRecord ->
                        bookSourceRecord.sequence = index
                    }
                    model.updateBookSourceRecordSeq(data)
                            .subscribe()
                            .destroy("fragment Book shelf update bookSourceRecord sequence")
                }
            }
        })
        mItemTouchHelper.attachToRecyclerView(bookList)

        model.books.observeOn(AndroidSchedulers.mainThread()).subscribe {
            adapter.data.clear()
            adapter.data.addAll(it)
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

        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_book_shelf, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.search_book_shelf -> {
                findNavController(this).navigate(R.id.searchFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private inner class Adapter : BaseAdapter() {
        val data: MutableList<BookShelfViewModel.BookShelfItemViewModel> = mutableListOf()

        override fun itemLayoutRes(viewType: Int): Int {
            return R.layout.item_book_list
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val bind = DataBindingUtil.bind<ItemBookListBinding>(holder.itemView)
            val viewModel = data.get(position)
            bind!!.model = viewModel
            viewModel.bookCover.onBackpressureLatest().observeOnMain().subscribe {
                Glide.with(this@BookshelfFragment)
                        .applyDefaultRequestOptions(requestOptions)
                        .load(it)
                        .into(holder.itemView.bookCover)
            }.destroy("fragment shelf book cover ${viewModel.id}")

            holder.itemView.setOnClickListener { v ->
                startActivity<ReadActivity>(ReadActivity.BOOK_NAME to viewModel.book.name, ReadActivity.BOOK_AUTHOR to viewModel.book.author)
            }
            holder.itemView.intro.setOnClickListener {
                startActivity<DetailsActivity>(DetailsActivity.BOOK_NAME to viewModel.book.name, DetailsActivity.BOOK_AUTHOR to viewModel.book.author)
            }
            holder.itemView.origin.setOnClickListener {
                ChooseBookSourceFragment.newInstance(viewModel.book.name, viewModel.book.author).show(fragmentManager!!)
            }
        }

        override fun getItemCount(): Int = data.size

        override fun getItemId(position: Int): Long {
            return data[position].id
        }
    }
}
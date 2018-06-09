package sjj.fiction.books

import android.app.ProgressDialog
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.facebook.drawee.view.SimpleDraweeView
import kotlinx.android.synthetic.main.fragment_books.*
import org.jetbrains.anko.find
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.indeterminateProgressDialog
import org.jetbrains.anko.support.v4.toast
import sjj.fiction.BaseFragment
import sjj.fiction.R
import sjj.fiction.model.BookGroup
import sjj.fiction.util.domain

/**
 * Created by SJJ on 2017/10/7.
 */
class BookrackFragment : BaseFragment(), BookrackContract.View {
    private lateinit var presenter: BookrackContract.Presenter
    private val adapter by lazy { Adapter() }
    private var loadingHint: ProgressDialog? = null
    private var loadingDetailsHint: ProgressDialog? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_books, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bookList.layoutManager = LinearLayoutManager(context)
        bookList.adapter = adapter
        BooksPresenter(this).start()

    }
    override fun onDestroyView() {
        super.onDestroyView()
        presenter.stop()
    }

    override fun setPresenter(presenter: BookrackContract.Presenter) {
        this.presenter = presenter
    }

    override fun setBookList(book: List<BookGroup>) {
        adapter.data = book
        adapter.notifyDataSetChanged()
    }

    override fun removeBook(book: BookGroup) {
        val list = adapter.data?.toMutableList()?:return
        list.removeAll { it.bookName == book.bookName && it.author == book.author }
        adapter.data = list
        adapter.notifyDataSetChanged()
    }

    override fun refreshBook(book: BookGroup) {
        val list = adapter.data?.toMutableList()?: mutableListOf()
        val group = list.indexOfFirst { it.bookName == book.bookName && it.author == book.author }
        if (group >= 0) {
            list.removeAt(group)
            list.add(group,book)
        } else {
            list.add(book)
        }
        adapter.data = list
        adapter.notifyDataSetChanged()
    }

    override fun setBookListLoadingHint(active: Boolean) {
//        loadingHint = if (active) {
//            loadingHint ?: indeterminateProgressDialog("正在加载书籍列表请稍候……")
//        } else {
//            loadingHint?.dismiss()
//            null
//        }
    }

    override fun setBookListLoadingError(e: Throwable) {
        toast("书籍加载出错：${e.message}")
    }

    override fun setDeleteBookError(e: Throwable) {
        toast("书籍删除出错：${e.message}")
    }

    override fun setBookDetailsLoadingHint(active: Boolean) {
        loadingDetailsHint = if (active) {
            loadingDetailsHint ?: indeterminateProgressDialog("正在加载书籍内容请稍候……")
        } else {
            loadingDetailsHint?.dismiss()
            null
        }
    }

    override fun setBookDetailsLoadingError(e: Throwable) {
        toast("书籍内容加载失败：${e.message}")
    }

    private inner class Adapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var data: List<BookGroup>? = null
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return object : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_book_list, parent, false)) {}
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val bookGroup = data!![position]
            holder.itemView.find<TextView>(R.id.bookName).text = bookGroup.bookName
            holder.itemView.find<TextView>(R.id.author).text = bookGroup.author
            holder.itemView.find<TextView>(R.id.originWebsite).text = bookGroup.currentBook.url.domain()
            holder.itemView.find<TextView>(R.id.lastChapter).text = bookGroup.currentBook.chapterList.last().chapterName
            holder.itemView.find<SimpleDraweeView>(R.id.bookCover).setImageURI(bookGroup.currentBook.bookCoverImgUrl)
//            holder.itemView.find<SimpleDraweeView>(R.id.bookCover).
            holder.itemView.setOnClickListener { v ->
                presenter.onSelectBook(bookGroup, v.context)
            }
            holder.itemView.setOnLongClickListener {
                alert {
                    title = "确认删除？"
                    message = "确认删除书籍：${bookGroup.bookName}？"
                    negativeButton("取消", {})
                    positiveButton("删除") {
                        presenter.deleteBook(bookGroup)
                    }
                }.show()
                true
            }
        }

        override fun getItemCount(): Int = data?.size ?: 0
    }
}
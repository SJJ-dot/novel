package sjj.fiction.details

import android.app.ProgressDialog
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.ViewHolder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_details.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.find
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.toast
import sjj.fiction.BaseActivity
import sjj.fiction.Details
import sjj.fiction.R
import sjj.fiction.model.Book
import sjj.fiction.model.BookGroup
import sjj.fiction.model.Chapter
import sjj.fiction.read.ReadActivity
import sjj.fiction.util.domain

/**
 * Created by SJJ on 2017/10/10.
 */
class DetailsActivity : BaseActivity(), DetailsContract.View {
    companion object {
        val book_url = "book_url"
    }

    private lateinit var presenter: DetailsContract.Presenter
    private var dialog: ProgressDialog? = null
    private var update: ProgressDialog? = null
    private lateinit var adapter: ChapterListAdapter
    private lateinit var bind: Details;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind =DataBindingUtil.setContentView<Details>(this,R.layout.activity_details)
//        setContentView(R.layout.activity_details)
        presenter = DetailsPresenter(intent.getStringExtra(data_book_name), intent.getStringExtra(data_book_author), this)
        bind.detailsPresenter = presenter
//        latestChapter.setOnClickListener {
//            presenter.onSelectChapter(it.tag.toString().toInt())
//        }
        originWebsite.setOnClickListener { presenter.onClickOrigin() }
        chapterList.layoutManager = LinearLayoutManager(this)
        adapter = ChapterListAdapter(presenter)
        chapterList.adapter = adapter
        chapterListButton.setOnClickListener {
            presenter.onClickChapterListBtn()
        }
        refreshBtn.setOnClickListener { presenter.checkUpdate() }
        presenter.start()
    }

    override fun onStop() {
        super.onStop()
        presenter.stop()
    }

    override fun setPresenter(presenter: DetailsContract.Presenter) {
        this.presenter = presenter
    }

    override fun showBookDetails(book: Book) {
        bind.book = book
//        bookName.text = book.name
//        author.text = book.author
        latestChapter.text = book.chapterList.last().chapterName
        latestChapter.tag = book.chapterList.size - 1
        intro.text = book.intro
        originWebsite.text = book.url.domain()
        bookCover.setImageURI(book.bookCoverImgUrl)
    }

    override fun showChapters(chapter: List<Chapter>, index: Int, active: Boolean) {
        if (active) {
            adapter.data.clear()
            adapter.data.addAll(chapter)
            adapter.notifyDataSetChanged()
            chapterList.scrollToPosition(index)
            chapterList.visibility = View.VISIBLE
        } else {
            chapterList.visibility = View.GONE
        }
    }

    override fun isShowChapters(): Boolean {
        return chapterList.visibility == View.VISIBLE
    }

    override fun setCheckUpdateIndicator(active: Boolean) {
        update = if (active) {
            update ?: indeterminateProgressDialog("正在检查更新请稍候……")
        } else {
            update?.dismiss()
            null
        }
    }

    override fun setLoadBookIndicator(active: Boolean) {
        dialog = if (active) {
            dialog ?: indeterminateProgressDialog("正在加载书籍请稍候……")
        } else {
            dialog?.dismiss()
            null
        }
    }

    override fun showErrorMessage(message: String) {
        toast(message)
    }

    override fun showBookContent(bookGroup: BookGroup, index: Int) {
        with(Intent(this, ReadActivity::class.java)) {
            putExtra(ReadActivity.DATA_BOOK_NAME, bookGroup.bookName)
            putExtra(ReadActivity.DATA_BOOK_AUTHOR, bookGroup.author)
            putExtra(ReadActivity.DATA_CHAPTER_INDEX, index)
            startActivity(this)
        }
    }

    override fun showBookOriginItems(items: List<String>) {
        alert {
            items(items) { dialog, index ->
                dialog.dismiss()
                presenter.onSelectBookOriginItem(index)
            }
        }.show()
    }

    private class ChapterListAdapter(val presenter: DetailsContract.Presenter) : RecyclerView.Adapter<ViewHolder>() {
        val data = mutableListOf<Chapter>()
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return object : ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_text_text, parent, false)) {}
        }

        override fun getItemCount() = data.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.itemView.find<TextView>(R.id.text1).text = data[position].chapterName
            holder.itemView.setOnClickListener {
                presenter.onSelectChapter(position)
            }
        }

    }
}
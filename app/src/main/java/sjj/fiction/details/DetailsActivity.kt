package sjj.fiction.details

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
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
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_details.*
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import sjj.fiction.BaseActivity
import sjj.fiction.Details
import sjj.fiction.R
import sjj.fiction.model.Book
import sjj.fiction.model.Chapter
import sjj.fiction.read.ReadActivity
import sjj.fiction.util.domain
import sjj.fiction.util.getModel

/**
 * Created by SJJ on 2017/10/10.
 */
class DetailsActivity : BaseActivity() {
    companion object {
        const val BOOK_NAME = "book_name"
        const val BOOK_AUTHOR = "book_author"
    }

    private val adapter by lazy { ChapterListAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bind = DataBindingUtil.setContentView<Details>(this, R.layout.activity_details)

        val model = getModel<DetailsViewModel>(object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return DetailsViewModel(intent.getStringExtra(BOOK_NAME), intent.getStringExtra(BOOK_AUTHOR)) as T
            }
        })
        model.book.observeOn(AndroidSchedulers.mainThread()).subscribe {
            bind.book = it
            originWebsite.text = it.url.domain()
            originWebsite.setOnClickListener { v ->
                v.isEnabled = false
                model.bookSource(it).doOnTerminate {
                    v.isEnabled = true
                }.subscribe { bs ->
                    v.isEnabled = true
                    alert {
                        items(bs.map { it.domain() }) { dialog, index ->
                            dialog.dismiss()
                            model.setBookSource(it, bs[index]).subscribe()
                        }
                    }.show()
                }
            }
            chapterListButton.setOnClickListener { v ->
                if (chapterList.visibility != View.VISIBLE) {
                    v.isEnabled = false
                    model.getReadIndex(it).doOnTerminate {
                        v.isEnabled = true
                    }.subscribe {index->
                        adapter.data.clear()
                        adapter.data.addAll(it.chapterList)
                        adapter.notifyDataSetChanged()
                        chapterList.scrollToPosition(index)
                        chapterList.visibility = View.VISIBLE
                    }
                } else {
                    chapterList.visibility = View.GONE
                }
            }
            refreshBtn.setOnClickListener {_-> model.refresh(it) }

            latestChapter.text = it.chapterList.last().chapterName
            latestChapter.setOnClickListener {_->
                startActivity<ReadActivity>(ReadActivity.BOOK_URL to it.url,ReadActivity.BOOK_INDEX to it.chapterList.lastIndex)
            }
            intro.text = it.intro
            bookCover.setImageURI(it.bookCoverImgUrl)

        }.destroy()

        chapterList.layoutManager = LinearLayoutManager(this)
        chapterList.adapter = adapter
    }


    private inner class ChapterListAdapter : RecyclerView.Adapter<ViewHolder>() {
        val data = mutableListOf<Chapter>()
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return object : ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_text_text, parent, false)) {}
        }

        override fun getItemCount() = data.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val book = data[position]
            holder.itemView.find<TextView>(R.id.text1).text = book.chapterName
            holder.itemView.setOnClickListener {
                startActivity<ReadActivity>(ReadActivity.BOOK_URL to book.url,ReadActivity.BOOK_INDEX to position)
            }
        }

    }
}
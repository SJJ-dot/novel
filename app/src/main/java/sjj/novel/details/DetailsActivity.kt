package sjj.novel.details

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.ViewHolder
import android.view.*
import android.widget.TextView
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_details.*
import org.jetbrains.anko.find
import org.jetbrains.anko.startActivity
import sjj.novel.BaseActivity
import sjj.novel.DISPOSABLE_ACTIVITY_DETAILS_REFRESH
import sjj.novel.R
import sjj.novel.databinding.ActivityDetailsBinding
import sjj.novel.model.Chapter
import sjj.novel.read.ReadActivity
import sjj.novel.util.lazyModel

/**
 * Created by SJJ on 2017/10/10.
 */
class DetailsActivity : BaseActivity() {
    companion object {
        const val BOOK_NAME = "book_name"
        const val BOOK_AUTHOR = "book_author"
    }

    private val model by lazyModel<DetailsViewModel> { arrayOf(intent.getStringExtra(BOOK_NAME), intent.getStringExtra(BOOK_AUTHOR)) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bind: ActivityDetailsBinding = DataBindingUtil.setContentView(this, R.layout.activity_details)

        setSupportActionBar(toolbar)
        val supportActionBar = supportActionBar!!
        supportActionBar.setDisplayHomeAsUpEnabled(true)

        val adapter = ChapterListAdapter()
        model.book.observeOn(AndroidSchedulers.mainThread()).subscribe { book ->
            bind.book = book
            originWebsite.text = book.origin?.sourceName
            originWebsite.setOnClickListener { v ->
                ChooseBookSourceFragment.newInstance(book.name, book.author).show(supportFragmentManager)
            }
            adapter.data = book.chapterList
            adapter.notifyDataSetChanged()

            detailsRefreshLayout.setOnRefreshListener {
                model.refresh(book).observeOn(AndroidSchedulers.mainThread()).doOnTerminate {
                    detailsRefreshLayout.isRefreshing = false
                }.subscribe().destroy(DISPOSABLE_ACTIVITY_DETAILS_REFRESH)
            }
            reading.setOnClickListener { _ ->
                model.bookSourceRecord.firstElement().observeOn(AndroidSchedulers.mainThread()).subscribe {
                    if (it.isThrough && it.readIndex == book.chapterList.size - 2) {
                        //有更新点击阅读直接进入下一章
                        model.setReadIndex(book.chapterList.last()).observeOn(AndroidSchedulers.mainThread()).subscribe {
                            startActivity<ReadActivity>(ReadActivity.BOOK_NAME to model.name, ReadActivity.BOOK_AUTHOR to model.author)
                        }.destroy("read book")

                    } else {
                        startActivity<ReadActivity>(ReadActivity.BOOK_NAME to model.name, ReadActivity.BOOK_AUTHOR to model.author)
                    }
                }.destroy("load book source record")
            }
            intro.text = book.intro
            bookCover.setImageURI(book.bookCoverImgUrl)
            if (book.chapterList.isNotEmpty()) {
                latestChapter.text = book.chapterList.last().chapterName
                latestChapter.setOnClickListener { v ->
                    v.isEnabled = false
                    model.setReadIndex(book.chapterList.last()).observeOn(AndroidSchedulers.mainThread()).doOnTerminate {
                        v.isEnabled = true
                    }.subscribe {
                        startActivity<ReadActivity>(ReadActivity.BOOK_NAME to model.name, ReadActivity.BOOK_AUTHOR to model.author)
                    }
                }
            } else {
                latestChapter.text = "无章节信息"
                latestChapter.isClickable = false
            }
        }.destroy("book details activity")

        chapterList.layoutManager = LinearLayoutManager(this)
        chapterList.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_details_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.chapter_list -> {
                model.bookSourceRecord.firstElement().observeOn(AndroidSchedulers.mainThread()).subscribe { index ->
                    chapterList.scrollToPosition(index.readIndex)
                    drawer_layout.openDrawer(Gravity.END)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(Gravity.END)) {
            drawer_layout.closeDrawer(Gravity.END)
        } else {
            super.onBackPressed()
        }
    }

    private inner class ChapterListAdapter : RecyclerView.Adapter<ViewHolder>() {
        var data = listOf<Chapter>()
        override fun getItemCount(): Int = data.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return object : ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_text_text, parent, false)) {}
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val book = data[position]
            holder.itemView.find<TextView>(R.id.text1).text = book.chapterName
            holder.itemView.setOnClickListener {
                it.isEnabled = false
                model.setReadIndex(book).observeOn(AndroidSchedulers.mainThread()).doOnTerminate {
                    it.isEnabled = true
                }.subscribe {
                    startActivity<ReadActivity>(ReadActivity.BOOK_NAME to model.name, ReadActivity.BOOK_AUTHOR to model.author)
                }
            }
        }
    }

}
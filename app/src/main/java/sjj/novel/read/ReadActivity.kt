package sjj.novel.read

import android.app.ProgressDialog
import android.arch.lifecycle.Observer
import android.graphics.Typeface
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.util.SparseBooleanArray
import android.view.*
import android.widget.TextView
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_read.*
import kotlinx.android.synthetic.main.item_read_chapter_content.view.*
import kotlinx.android.synthetic.main.item_read_chapter_content_text_line.view.*
import org.jetbrains.anko.ctx
import org.jetbrains.anko.find
import org.jetbrains.anko.progressDialog
import org.jetbrains.anko.toast
import sjj.alog.Log
import sjj.novel.*
import sjj.novel.model.BookSourceRecord
import sjj.novel.model.Chapter
import sjj.novel.util.lazyModel
import sjj.novel.util.observeOnMain
import sjj.novel.view.reader.bean.BookBean
import sjj.novel.view.reader.bean.BookRecordBean
import sjj.novel.view.reader.page.PageLoader
import sjj.novel.view.reader.page.PageMode
import sjj.novel.view.reader.page.PageView
import sjj.novel.view.reader.page.TxtChapter
import kotlin.math.max
import kotlin.math.min

class ReadActivity : BaseActivity() {
    companion object {
        val BOOK_NAME = "BOOK_NAME"
        val BOOK_AUTHOR = "BOOK_AUTHOR"
    }

    private var loadBookHint: ProgressDialog? = null
    private var cached: ProgressDialog? = null

    private val model by lazyModel<ReadViewModel> { arrayOf(intent.getStringExtra(BOOK_NAME), intent.getStringExtra(BOOK_AUTHOR)) }

//    private val contentAdapter = ChapterContentAdapter()

    private val pageLoader by lazy { chapterContent.pageLoader }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_read)
        setSupportActionBar(toolbar)
        val supportActionBar = supportActionBar!!
        supportActionBar.setDisplayHomeAsUpEnabled(true)
        supportActionBar.hide()
        val chapterListAdapter = ChapterListAdapter()
//        chapterContent.adapter = contentAdapter
        chapterList.adapter = chapterListAdapter

        chapterContent.setTouchListener {
            Log.e("Touch center")
            if (supportActionBar.isShowing) {
                supportActionBar.hide()
            } else {
                supportActionBar.show()
            }
        }


        model.book.firstElement().observeOn(AndroidSchedulers.mainThread()).subscribe { book ->
            title = book.name
            chapterListAdapter.data = book.chapterList
            chapterListAdapter.notifyDataSetChanged()

            //阅读记录
            model.readIndex.firstElement().observeOn(AndroidSchedulers.mainThread()).subscribe {
                val index = min(max(book.chapterList.lastIndex, 0), it.readIndex)
                chapterList.scrollToPosition(index)

                pageLoader.setBookRecord(BookRecordBean().apply {
                    bookId = book.url
                    chapter = it.readIndex
                    pagePos = it.pagePos
                    isThrough = it.isThrough
                })

                pageLoader.setBook(BookBean().apply {
                    id = book.url
                    title = book.name
                    author = book.author
                    shortIntro = book.intro
                    cover = book.bookCoverImgUrl
                    bookChapterList = book.chapterList.map { chapter ->
                        TxtChapter().apply {
                            this.bookId = book.url
                            this.link = chapter.url
                            this.title = chapter.chapterName
                            this.content = chapter.content
                        }
                    }

                })

                pageLoader.setOnPageChangeListener(object : PageLoader.OnPageChangeListener {
                    override fun onBookRecordChange(bean: BookRecordBean) {
                        model.setReadIndex(book.chapterList[bean.chapter], bean.pagePos, bean.isThrough).subscribe()
                    }

                    override fun onChapterChange(pos: Int) {
                    }

                    override fun requestChapters(requestChapters: MutableList<TxtChapter>) {
                        model.getChapter(requestChapters).observeOnMain().subscribe({
                            pageLoader.openChapter()
                        }, {
                            pageLoader.chapterError()
                        }).destroy("requestChapters")
                    }

                    override fun onCategoryFinish(chapters: MutableList<TxtChapter>?) {
                    }

                    override fun onPageCountChange(count: Int) {
                    }

                    override fun onPageChange(pos: Int) {
                    }

                })

                pageLoader.refreshChapterList()
            }.destroy(DISPOSABLE_ACTIVITY_READ_READ_INDEX)


        }.destroy()
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_read_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.menu_cached -> {

                model.book.firstElement().observeOn(AndroidSchedulers.mainThread()).subscribe {
                    cached?.dismiss()
                    cached = progressDialog("正在缓存章节内容")

                    model.cachedBookChapter(it.url).observeOn(AndroidSchedulers.mainThread()).subscribe({ p: Pair<Int, Int> ->
                        cached?.max = p.second
                        cached?.progress = p.first
                    }, {
                        toast("缓存章节内容出错：$it")
                        cached?.dismiss()
                        cached = null
                    }, {
                        toast("缓存章节内容完成")
                        cached?.dismiss()
                        cached = null
                    }).destroy(DISPOSABLE_CACHED_BOOK_CHAPTER)
                }.destroy(DISPOSABLE_CACHED_BOOK_CHAPTER)
                true
            }
            R.id.menu_flip_mode -> {
                AlertDialog.Builder(this).setSingleChoiceItems(arrayOf("仿真","覆盖","平移","无","滚动"), PageMode.valueOf(AppConfig.flipPageMode).ordinal) { dialog, which ->
                    dialog.dismiss()
                    val mode = PageMode.values()[which]
                    AppConfig.flipPageMode = mode.name
                    pageLoader.setPageMode(mode)
                }.show()
                true
            }
            R.id.menu_add -> {
                pageLoader.setTextSizeIncrease(true)
                true
            }
            R.id.menu_minus -> {
                pageLoader.setTextSizeIncrease(false)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    override fun onPause() {
        super.onPause()
        pageLoader.saveRecord()
    }

    private inner class ChapterListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        var data = listOf<Chapter>()

        override fun getItemCount(): Int = data.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return object : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_text_text, parent, false)) {}
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val c = data[position]
            holder.itemView.find<TextView>(R.id.text1).text = c.chapterName
            holder.itemView.setOnClickListener {
                model.setReadIndex(c, 0).subscribe().destroy(DISPOSABLE_READ_INDEX)
                pageLoader.skipToChapter(position)
//                chapterContent.scrollToPosition(position)
            }
        }

    }
}

package sjj.novel.read

import android.app.ProgressDialog
import android.arch.lifecycle.Observer
import android.graphics.Typeface
import android.os.Bundle
import android.support.v4.widget.DrawerLayout
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
import sjj.novel.view.reader.page.PageLoader.STATUS_LOADING
import sjj.novel.view.reader.page.PageMode
import sjj.novel.view.reader.page.PageView
import sjj.novel.view.reader.page.TxtChapter
import kotlin.math.max
import kotlin.math.min

class ReadActivity : BaseActivity(), ReaderSettingFragment.CallBack {
    companion object {
        val BOOK_NAME = "BOOK_NAME"
        val BOOK_AUTHOR = "BOOK_AUTHOR"
    }

    private var cached: ProgressDialog? = null

    private val model by lazyModel<ReadViewModel> { arrayOf(intent.getStringExtra(BOOK_NAME), intent.getStringExtra(BOOK_AUTHOR)) }

    private val mPageLoader by lazy { chapterContent.pageLoader }

    private val menuFragment by lazy { ReaderSettingFragment() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_read)
        setSupportActionBar(toolbar)
        val supportActionBar = supportActionBar!!
        supportActionBar.setDisplayHomeAsUpEnabled(true)
        supportActionBar.hide()
        //禁止手势滑出
        drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

        val chapterListAdapter = ChapterListAdapter()
//        chapterContent.adapter = contentAdapter
        chapterList.adapter = chapterListAdapter

        chapterContent.setTouchListener {
            toggleMenu()
        }


        model.book.firstElement().observeOn(AndroidSchedulers.mainThread()).subscribe { book ->
            title = book.name
            chapterListAdapter.data = book.chapterList
            chapterListAdapter.notifyDataSetChanged()

            //阅读记录
            model.readIndex.firstElement().observeOn(AndroidSchedulers.mainThread()).subscribe {
                val index = min(max(book.chapterList.lastIndex, 0), it.readIndex)
                chapterList.scrollToPosition(index)

                mPageLoader.setBookRecord(BookRecordBean().apply {
                    bookId = book.url
                    chapter = it.readIndex
                    pagePos = it.pagePos
                    isThrough = it.isThrough
                })

                mPageLoader.setBook(BookBean().apply {
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
                        }
                    }

                })

                mPageLoader.setOnPageChangeListener(object : PageLoader.OnPageChangeListener {
                    override fun onBookRecordChange(bean: BookRecordBean) {
                        model.setReadIndex(book.chapterList[bean.chapter], bean.pagePos, bean.isThrough).subscribe()
                    }

                    override fun onChapterChange(pos: Int) {
                    }

                    override fun requestChapters(requestChapters: MutableList<TxtChapter>) {
                        model.getChapter(requestChapters).observeOnMain().subscribe({
                            if (mPageLoader.pageStatus == STATUS_LOADING)
                                mPageLoader.openChapter()
                        }, {
                            mPageLoader.chapterError()
                        }).destroy("requestChapters")
                    }

                    override fun onCategoryFinish(chapters: MutableList<TxtChapter>?) {
                    }

                    override fun onPageCountChange(count: Int) {
                        menuFragment.setPageCount(count)
                    }

                    override fun onPageChange(pos: Int) {
                        menuFragment.setPagePos(pos)
                    }

                })

                mPageLoader.refreshChapterList()
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
                AlertDialog.Builder(this).setSingleChoiceItems(arrayOf("仿真", "覆盖", "平移", "无", "滚动"), PageMode.valueOf(AppConfig.flipPageMode).ordinal) { dialog, which ->
                    dialog.dismiss()
                    val mode = PageMode.values()[which]
                    AppConfig.flipPageMode = mode.name
                    mPageLoader.setPageMode(mode)
                }.show()
                true
            }
            R.id.menu_add -> {
                mPageLoader.setTextSizeIncrease(true)
                true
            }
            R.id.menu_minus -> {
                mPageLoader.setTextSizeIncrease(false)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        if (drawer_layout?.isDrawerOpen(Gravity.START) == true) {
            drawer_layout?.closeDrawers()
        }else if (supportActionBar?.isShowing == true) {
            toggleMenu()
        } else {
            super.onBackPressed()
        }
    }

    private fun toggleMenu() {
        if (supportActionBar?.isShowing == true) {
            supportActionBar?.hide()
            supportFragmentManager.beginTransaction().remove(menuFragment).commitAllowingStateLoss()
        } else {
            supportActionBar?.show()
            supportFragmentManager.beginTransaction().replace(R.id.menu_fragment_container, menuFragment).commit()
        }
    }

    override fun onPause() {
        super.onPause()
        mPageLoader.saveRecord()
    }

    /**
     * 底部菜单回调
     */
    override fun openChapterList() {
        drawer_layout.openDrawer(Gravity.START)
    }

    /**
     * 底部菜单回调
     */
    override fun getPageLoader(): PageLoader? {
        return mPageLoader
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
                mPageLoader.skipToChapter(position)
//                chapterContent.scrollToPosition(position)
            }
        }

    }
}

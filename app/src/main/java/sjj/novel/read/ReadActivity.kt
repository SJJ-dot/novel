package sjj.novel.read

import android.app.ProgressDialog
import android.os.Bundle
import android.support.v4.widget.DrawerLayout
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.TextView
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_read.*
import org.jetbrains.anko.find
import org.jetbrains.anko.progressDialog
import org.jetbrains.anko.toast
import sjj.novel.*
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

        chapterContent.setTouchListener(object : PageView.TouchListener {
            override fun intercept(event: MotionEvent?): Boolean {
                val showing = supportActionBar.isShowing
                if (showing && event?.action == MotionEvent.ACTION_DOWN) {
                    return true
                }
                if (showing && event?.action == MotionEvent.ACTION_UP) {
                    toggleMenu()
                    return true
                }
                return showing
            }

            override fun center() {
                toggleMenu()
            }

        })


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

                mPageLoader.book = BookBean().apply {
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

                }

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


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_read_menu, menu)
        val subMenu = menu.addSubMenu("翻页模式")
        for (m in PageMode.values()) {
            subMenu.add(0, m.ordinal, 0, m.des)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
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
            R.id.menu_refresh -> {
                mPageLoader.curChapter?.also {
                    model.getChapter(listOf(it), true).observeOnMain().subscribe({ txtChapter ->
                        mPageLoader.refreshChapter(txtChapter.first())
                    }, {
                        toast("刷新失败")
                    }).destroy("requestChapters menu_refresh")
                }
                true
            }
            else -> when {
                item.itemId < PageMode.values().size && item.itemId >= 0 -> {
                    val mode = PageMode.values()[item.itemId]
                    AppConfig.flipPageMode = mode.name
                    mPageLoader.setPageMode(mode)
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onBackPressed() {
        if (drawer_layout?.isDrawerOpen(Gravity.START) == true) {
            drawer_layout?.closeDrawers()
        } else if (supportActionBar?.isShowing == true) {
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
        drawer_layout.openDrawer(Gravity.END)
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

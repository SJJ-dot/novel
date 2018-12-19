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
import sjj.novel.model.Chapter
import sjj.novel.util.lazyModel
import sjj.novel.util.observeOnMain
import sjj.novel.view.reader.bean.BookBean
import sjj.novel.view.reader.bean.BookRecordBean
import sjj.novel.view.reader.page.PageLoader
import sjj.novel.view.reader.page.PageView
import sjj.novel.view.reader.page.TxtChapter
import kotlin.math.max
import kotlin.math.min

class ReadActivity : BaseActivity() {
    companion object {
        val BOOK_NAME = "BOOK_NAME"
        val BOOK_AUTHOR = "BOOK_AUTHOR"
    }

    private val ttfs = arrayOf<String>("Roboto-Black.ttf", "Roboto-BlackItalic.ttf", "Roboto-Bold.ttf", "Roboto-BoldItalic.ttf", "Roboto-Italic.ttf", "Roboto-Light.ttf", "Roboto-LightItalic.ttf", "Roboto-Medium.ttf", "Roboto-MediumItalic.ttf", "Roboto-Regular.ttf", "Roboto-Thin.ttf",
            "Roboto-ThinItalic.ttf", "RobotoCondensed-Bold.ttf", "RobotoCondensed-BoldItalic.ttf", "RobotoCondensed-Italic.ttf", "RobotoCondensed-Light.ttf",
            "RobotoCondensed-LightItalic.ttf",
            "RobotoCondensed-Regular.ttf")

    private var loadBookHint: ProgressDialog? = null
    private var cached: ProgressDialog? = null

    private val model by lazyModel<ReadViewModel> { arrayOf(intent.getStringExtra(BOOK_NAME), intent.getStringExtra(BOOK_AUTHOR)) }

//    private val contentAdapter = ChapterContentAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_read)
        setSupportActionBar(toolbar)
        val supportActionBar = supportActionBar!!
        supportActionBar.setDisplayHomeAsUpEnabled(true)

        val chapterListAdapter = ChapterListAdapter()
//        chapterContent.adapter = contentAdapter
        chapterList.adapter = chapterListAdapter

        val pageLoader = chapterContent.pageLoader
        chapterContent.setTouchListener(object : PageView.TouchListener {
            override fun onTouch(): Boolean {
                return true
            }

            override fun center() {
            }

            override fun prePage() {
            }

            override fun nextPage() {
            }

            override fun cancel() {
            }

        })
        pageLoader.setOnPageChangeListener(object : PageLoader.OnPageChangeListener {
            override fun onBookRecordChange(bean: BookRecordBean) {
            }

            override fun onChapterChange(pos: Int) {
            }

            override fun requestChapters(requestChapters: MutableList<TxtChapter>) {
                model.getChapter(requestChapters).observeOnMain().subscribe({
                    Log.e("requestChapters success")
                    pageLoader.openChapter()
                }, {
                    Log.e("requestChapters chapterError")
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

        model.book.firstElement().observeOn(AndroidSchedulers.mainThread()).subscribe { book ->
            title = book.name
            seekBar.max = book.chapterList.size
            chapterListAdapter.data = book.chapterList
            chapterListAdapter.notifyDataSetChanged()

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
            pageLoader.refreshChapterList()

            //阅读记录
            model.readIndex.firstElement().observeOn(AndroidSchedulers.mainThread()).subscribe {
                val index = min(max(book.chapterList.lastIndex, 0), it.readIndex)
                chapterList.scrollToPosition(index)
//                chapterContent.scrollToPosition(index)
            }.destroy(DISPOSABLE_ACTIVITY_READ_READ_INDEX)
        }.destroy()
//        chapterContent.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
//                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
//                    val manager = recyclerView.layoutManager as LinearLayoutManager
//                    var position = manager.findFirstVisibleItemPosition()
//                    seekBar.progress = position
//
//                    val b = recyclerView.computeVerticalScrollExtent() + recyclerView.computeVerticalScrollOffset() - recyclerView.computeVerticalScrollRange() >= -chapterContent.height / 4 &&
//                            contentAdapter.isLoadContent[position]
//                    if (contentAdapter.data.size > position) {
//
//                        if (b) {
//                            position = manager.findLastVisibleItemPosition()
//                            seekBar.progress = position
//                        }
//                        val chapter = contentAdapter.data[position]
//                        chapterName.text = chapter.chapterName
//
//                        model.setReadIndex(chapter, b).subscribe().destroy(DISPOSABLE_READ_INDEX)
//                    }
//                }
//            }
//        })


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
            R.id.menu_ttf -> {
                AlertDialog.Builder(this).setSingleChoiceItems(ttfs, ttfs.indexOf(AppConfig.ttf.value)) { dialog, which ->
                    dialog.dismiss()
                    AppConfig.ttf.value = ttfs[which]
//                    contentAdapter.ttf = Typeface.createFromAsset(assets, "fonts/${ttfs[which]}")
//                    contentAdapter.notifyDataSetChanged()
                }.show()
                true
            }
            R.id.menu_add -> {
                AppConfig.readChapterTextSize.value = AppConfig.readChapterTextSize.value!! + 1
                true
            }
            R.id.menu_minus -> {
                AppConfig.readChapterTextSize.value = AppConfig.readChapterTextSize.value!! - 1
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    inner class ChapterContentAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        var data = listOf<Chapter>()

        val isLoadContent = SparseBooleanArray()

        lateinit var ttf: Typeface

        var contentTextSize: Float = 0f

        init {
            AppConfig.ttf.observe(this@ReadActivity, Observer {
                ttf = Typeface.createFromAsset(ctx.assets, "fonts/$it")
                notifyDataSetChanged()
            })
            AppConfig.readChapterTextSize.observe(this@ReadActivity, Observer {
                contentTextSize = it!!
                notifyDataSetChanged()
            })
        }

        override fun getItemCount(): Int = data.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return object : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_read_chapter_content, parent, false)) {}
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            isLoadContent.put(position, false)
            val chapter = data[position]
            holder.itemView.readItemChapterContentTitle.text = chapter.chapterName
            fun bindContent() {
                holder.itemView.apply {
                    readItemChapterContentHint.apply {
                        text = "加载中请稍后……"
                        visibility = View.VISIBLE
                    }
                    readItemChapterContentText.visibility = View.GONE
                    layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                }

                model.getChapter(chapter.url).observeOn(AndroidSchedulers.mainThread()).subscribe({
                    chapter.isLoadSuccess = it.isLoadSuccess
//                    val lineAdapter = TextLineAdapter(Html.fromHtml(it.content).split("\n").mapIndexed { index, s -> TextLine(s, index, chapter.index) })
//                    holder.itemView.readItemChapterContentLines.adapter = lineAdapter
//                    lineAdapter.typeface = ttf;
//                    lineAdapter.textSize = contentTextSize
                    holder.itemView.apply {
                        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT

                        readItemChapterContentHint.visibility = View.GONE
                        readItemChapterContentText.apply {
                            visibility = View.VISIBLE
                            typeface = ttf
                            textSize = contentTextSize
                            text = Html.fromHtml(it.content)
                        }
                    }
                    isLoadContent.put(position, true)
//                    holder.itemView.readItemChapterContentLines.visibility = View.VISIBLE
                }, { _ ->
                    holder.itemView.readItemChapterContentHint.text = "加载失败，点击重试……"
                    holder.itemView.readItemChapterContentHint.setOnClickListener {
                        bindContent()
                        holder.itemView.readItemChapterContentHint.isClickable = false
                    }
                    holder.itemView.readItemChapterContentHint.visibility = View.VISIBLE
//                    holder.itemView.readItemChapterContentLines.visibility = View.GONE
                }).destroy("${holder.itemView}")
            }
            bindContent()

        }

    }


    class TextLineAdapter(var data: List<TextLine>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var typeface: Typeface? = null
        var textSize: Float? = null
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return object : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_read_chapter_content_text_line, parent, false)) {

            }
        }

        override fun getItemCount(): Int = data.size

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            holder.itemView.readItemChapterContentLine.text = data[position].text
            typeface?.also {
                holder.itemView.readItemChapterContentLine.typeface = it
            }
            textSize?.also {
                holder.itemView.readItemChapterContentLine.textSize = it
            }
        }
    }

    class TextLine(var text: String, var lineIndex: Int, var chapterIndex: Int)

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
                model.setReadIndex(c).subscribe().destroy(DISPOSABLE_READ_INDEX)
//                chapterContent.scrollToPosition(position)
            }
        }

    }
}

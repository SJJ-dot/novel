package sjj.fiction.read

import android.app.ProgressDialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.paging.PagedListAdapter
import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.util.DiffUtil
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE
import android.text.Html
import android.view.*
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_read.*
import kotlinx.android.synthetic.main.item_read_chapter_content.view.*
import kotlinx.android.synthetic.main.item_read_chapter_content_text_line.view.*
import org.jetbrains.anko.*
import sjj.alog.Log
import sjj.fiction.*
import sjj.fiction.model.Chapter
import sjj.fiction.util.getModel
import sjj.fiction.util.log
import sjj.fiction.util.lparams
import sjj.fiction.util.toDpx

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

    private val model by lazy {
        getModel<ReadViewModel>(object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return ReadViewModel(intent.getStringExtra(BOOK_NAME), intent.getStringExtra(BOOK_AUTHOR)) as T
            }
        })
    }

    private val contentAdapter = ChapterContentAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_read)
        setSupportActionBar(toolbar)
        val supportActionBar = supportActionBar!!
        supportActionBar.setDisplayHomeAsUpEnabled(true)

        val chapterListAdapter = ChapterListAdapter()
        chapterContent.adapter = contentAdapter
        chapterList.adapter = chapterListAdapter
        model.book.firstElement().observeOn(AndroidSchedulers.mainThread()).subscribe {
            title = it.name
            seekBar.max = it.chapterList.size
            contentAdapter.data = it.chapterList
            chapterListAdapter.data = it.chapterList
            contentAdapter.notifyDataSetChanged()
            chapterListAdapter.notifyDataSetChanged()
            model.readIndex.firstElement().observeOn(AndroidSchedulers.mainThread()).subscribe {
                chapterList.scrollToPosition(it)
                chapterContent.scrollToPosition(it)
            }.destroy(DISPOSABLE_ACTIVITY_READ_READ_INDEX)
        }.destroy()
        chapterContent.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val manager = recyclerView.layoutManager as LinearLayoutManager
                val position = manager.findFirstVisibleItemPosition()
                seekBar.progress = position
                if (contentAdapter.data.size > position) {
                    chapterName.text = contentAdapter.data[position].chapterName
                    model.setReadIndex(position).subscribe().destroy(DISPOSABLE_READ_INDEX)
                }
            }
        })
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
                    contentAdapter.ttf = Typeface.createFromAsset(assets, "fonts/${ttfs[which]}")
                    contentAdapter.notifyDataSetChanged()
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
            val chapter = data[position]
            holder.itemView.readItemChapterContentTitle.text = chapter.chapterName
            fun bindContent() {
                holder.itemView.readItemChapterContentHint.text = "加载中请稍后……"

                model.getChapter(chapter.url).observeOn(AndroidSchedulers.mainThread()).subscribe({
                    chapter.isLoadSuccess = it.isLoadSuccess
                    val lineAdapter = TextLineAdapter(Html.fromHtml(it.content).split("\n").mapIndexed { index, s -> TextLine(s, index, chapter.index) })
                    holder.itemView.readItemChapterContentLines.adapter = lineAdapter
                    lineAdapter.typeface = ttf;
                    lineAdapter.textSize = contentTextSize

                    holder.itemView.readItemChapterContentHint.visibility =View.GONE
                    holder.itemView.readItemChapterContentLines.visibility = View.VISIBLE
                }, { _ ->
                    holder.itemView.readItemChapterContentHint.text = "加载失败，点击重试……"
                    holder.itemView.readItemChapterContentHint.setOnClickListener {
                        bindContent()
                        holder.itemView.readItemChapterContentHint.isClickable = false
                    }
                    holder.itemView.readItemChapterContentHint.visibility =View.VISIBLE
                    holder.itemView.readItemChapterContentLines.visibility = View.GONE
                }).destroy("${holder.itemView}")
            }
            bindContent()

        }

    }


    class TextLineAdapter(var data: List<TextLine>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var typeface: Typeface?=null
        var textSize: Float? = null
        init {
            Log.e(data)
        }
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
                model.setReadIndex(position).subscribe().destroy(DISPOSABLE_READ_INDEX)
                chapterContent.scrollToPosition(position)
            }
        }

    }
}

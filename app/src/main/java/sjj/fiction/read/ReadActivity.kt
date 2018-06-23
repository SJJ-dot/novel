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
import android.text.Html
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_read.*
import org.jetbrains.anko.*
import sjj.alog.Log
import sjj.fiction.*
import sjj.fiction.model.Chapter
import sjj.fiction.util.getModel
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

        model.book.subscribe {
            title = it.name
            model.getChapters(it.url).observe(this, Observer {
                Log.e(it?.size)
                seekBar.max = it?.size ?:0
                contentAdapter.submitList(it)
                chapterListAdapter.submitList(it)
                model.readIndex.subscribe {
                    chapterList.scrollToPosition(it)
                    chapterContent.scrollToPosition(it)
                    seekBar.progress = it
                }.destroy(DISPOSABLE_ACTIVITY_READ_READ_INDEX)
            })

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

                model.book.firstElement().subscribe {
                    cached?.dismiss()
                    cached = progressDialog("正在缓存章节内容")
                    cached?.max = 1000
                    model.cachedBookChapter(it.url).subscribe({ _ ->
                        cached?.progress = cached?.progress ?: 0 + 1
                    }, {
                        toast("缓存章节内容出错：$it")
                        cached?.dismiss()
                        cached = null
                    },{
                        toast("缓存章节内容完成")
                        cached?.dismiss()
                        cached = null
                    })
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


    inner class ChapterContentAdapter : PagedListAdapter<Chapter, RecyclerView.ViewHolder>(object : DiffUtil.ItemCallback<Chapter>() {
        override fun areItemsTheSame(oldItem: Chapter?, newItem: Chapter?) = oldItem?.url == newItem?.url

        override fun areContentsTheSame(oldItem: Chapter?, newItem: Chapter?) = oldItem?.content == newItem?.content
    }) {
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

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return object : RecyclerView.ViewHolder(with(parent.context) {
                verticalLayout {
                    textView {
                        id = R.id.readItemChapterContentTitle
                        setPadding(16.toDpx(), 8.toDpx(), 16.toDpx(), 8.toDpx())
                        textSize = 24f
                        textColor = resources.getColor(R.color.material_textBlack_text)
                        setBackgroundColor(resources.getColor(R.color.chapter_background))
                    }
                    include<TextView>(R.layout.item_read_chapter_content)
                }.lparams<RecyclerView.LayoutParams, LinearLayout> {
                    width = RecyclerView.LayoutParams.MATCH_PARENT
                    height = RecyclerView.LayoutParams.MATCH_PARENT
                }
            }) {}
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val chapter = getItem(position) ?: return

            chapterName.text = chapter.chapterName

            holder.itemView.findViewById<TextView>(R.id.readItemChapterContentTitle).text = chapter.chapterName
            if (chapter.content?.isNotEmpty() == true) {
                val content = holder.itemView.findViewById<TextView>(R.id.readItemChapterContent)
                content.typeface = ttf
                content.textSize = contentTextSize
                content.text = Html.fromHtml(chapter.content)
            }
            if (!chapter.isLoadSuccess || chapter.content?.isEmpty() == true) {
                model.loadChapter(chapter)
                holder.itemView.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            } else {
                holder.itemView.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            }
        }

    }

    private inner class ChapterListAdapter : PagedListAdapter<Chapter, RecyclerView.ViewHolder>(object : DiffUtil.ItemCallback<Chapter>() {
        override fun areItemsTheSame(oldItem: Chapter?, newItem: Chapter?) = oldItem?.url == newItem?.url

        override fun areContentsTheSame(oldItem: Chapter?, newItem: Chapter?) = oldItem?.chapterName == newItem?.chapterName
    }) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return object : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_text_text, parent, false)) {}
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val c = getItem(position) ?: return
            holder.itemView.find<TextView>(R.id.text1).text = c.chapterName
            holder.itemView.setOnClickListener {
                model.setReadIndex(position).subscribe().destroy(DISPOSABLE_READ_INDEX)
            }
        }

    }
}

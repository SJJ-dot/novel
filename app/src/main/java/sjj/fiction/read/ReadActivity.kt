package sjj.fiction.read

import android.os.Bundle
import android.support.v4.widget.DrawerLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import com.raizlabs.android.dbflow.kotlinextensions.save
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_read.*
import org.jetbrains.anko.*
import sjj.alog.Log
import sjj.fiction.BaseActivity
import sjj.fiction.R
import sjj.fiction.data.Repository.FictionDataRepository
import sjj.fiction.model.Book
import sjj.fiction.model.BookGroup
import sjj.fiction.model.Chapter
import sjj.fiction.util.fictionDataRepository
import sjj.fiction.util.lparams
import sjj.fiction.util.textView
import sjj.fiction.util.toDpx

class ReadActivity : BaseActivity() {
    companion object {
        val DATA_BOOK = "DATA_BOOK"
        val DATA_CHAPTER_INDEX = "DATA_CHAPTER_INDEX"
    }

    private val com = CompositeDisposable()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_read)
        setSupportActionBar(toolbar)
        val supportActionBar = supportActionBar!!
        supportActionBar.setDisplayHomeAsUpEnabled(true)
        val bookGroup = intent.getSerializableExtra(DATA_BOOK) as BookGroup
        val book = bookGroup.currentBook
        title = book.name
        chapterContent.layoutManager = LinearLayoutManager(this)
        chapterContent.adapter = ChapterContentAdapter(book.chapterList)
        val current = Math.min(intent.getIntExtra(DATA_CHAPTER_INDEX, 0), book.chapterList.size - 1)
        chapterName.text = book.chapterList[current].chapterName
        chapterContent.scrollToPosition(current)
        chapterList.layoutManager = LinearLayoutManager(this)
        chapterList.adapter = ChapterListAdapter(book)
        drawer_layout.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
            override fun onDrawerOpened(drawerView: View?) {
                val manager = chapterContent.layoutManager as LinearLayoutManager
                val position = manager.findFirstVisibleItemPosition()
                chapterList.scrollToPosition(position)
            }
        })
        chapterContent.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val manager = chapterContent.layoutManager as LinearLayoutManager
                val position = manager.findFirstVisibleItemPosition()
                if (chapterName.tag != position) {
                    chapterName.text = book.chapterList[position].chapterName
                    chapterName.tag = position
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        val adapter = chapterContent.adapter as ChapterContentAdapter
        adapter.clear()
        val manager = chapterContent.layoutManager as LinearLayoutManager
        val position = manager.findFirstVisibleItemPosition()
        val bookGroup = intent.getSerializableExtra(DATA_BOOK) as BookGroup
        bookGroup.readIndex = position
        bookGroup.save()
        com.clear()
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
                val book = (intent.getSerializableExtra(DATA_BOOK) as BookGroup).currentBook
                val dialog = progressDialog("正在缓存章节列表") {
                    max = book.chapterList.size
                }
                com.add(fictionDataRepository.cachedBookChapter(book)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            dialog.progress = dialog.progress + 1
                        }, {
                            toast("缓存出错")

                        }, {
                            dialog.dismiss()
                            toast("加载完成：${dialog.progress}")
                        }))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private class ChapterContentAdapter(val chapters: List<Chapter>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        private val fiction: FictionDataRepository = fictionDataRepository
        private var compDisposable: CompositeDisposable = CompositeDisposable()
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return object : RecyclerView.ViewHolder(with(parent.context) {
                verticalLayout {
                    textView {
                        id = R.id.readItemChapterContentTitle
                        setPadding(16.toDpx(), 8.toDpx(), 16.toDpx(), 8.toDpx())
                        textSize = 24f
                        textColor = getColor(R.color.material_textBlack_text)
                    }
                    textView {
                        id = R.id.readItemChapterContent
                        setPadding(16.toDpx(), 8.toDpx(), 16.toDpx(), 8.toDpx())
                        textSize = 20f
                        textColor = getColor(R.color.material_textBlack_text)
                    }
                }.lparams<RecyclerView.LayoutParams, LinearLayout> {
                    width = RecyclerView.LayoutParams.MATCH_PARENT
                    height = RecyclerView.LayoutParams.MATCH_PARENT
                }
            }) {}
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val chapter = chapters[position]
            holder.itemView.findViewById<TextView>(R.id.readItemChapterContentTitle).text = chapter.chapterName
            if (chapter.content.content.isNotEmpty()) {
                holder.itemView.findViewById<TextView>(R.id.readItemChapterContent).text = Html.fromHtml(chapter.content.content)
            }
            if (!chapter.isLoadSuccess || chapter.content.content.isEmpty()) {
                if (!chapter.isLoading) {
                    chapter.isLoading = true
                    compDisposable.add(fiction.loadBookChapter(chapter)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({
                                it.isLoading = false
                                notifyDataSetChanged()
                            }, {
                                chapter.isLoading = false
                                chapter.content.content = "章节加载失败：${it.message}"
                                notifyDataSetChanged()
                            }))
                }
                holder.itemView.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            } else {
                holder.itemView.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            }
        }

        override fun getItemCount(): Int = chapters.size
        fun clear() {
            compDisposable.clear()
        }
    }

    private inner class ChapterListAdapter(val book: Book) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        val data = book.chapterList
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return object : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_text_text, parent, false)) {}
        }

        override fun getItemCount() = data.size

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            holder.itemView.find<TextView>(R.id.text1).text = data[position].chapterName
            holder.itemView.setOnClickListener {
                chapterContent.scrollToPosition(position)
            }
        }

    }
}

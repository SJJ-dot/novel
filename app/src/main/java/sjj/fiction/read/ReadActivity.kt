package sjj.fiction.read

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.widget.DrawerLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_read.*
import org.jetbrains.anko.find
import sjj.alog.Log
import sjj.fiction.BaseActivity
import sjj.fiction.R
import sjj.fiction.data.Repository.FictionDataRepository
import sjj.fiction.data.Repository.impl.FictionDataRepositoryImpl
import sjj.fiction.model.Book
import sjj.fiction.model.Chapter
import sjj.fiction.util.textView
import sjj.fiction.util.toDpx

class ReadActivity : BaseActivity() {
    companion object {
        val DATA_BOOK = "DATA_BOOK"
        val DATA_CHAPTER_INDEX = "DATA_CHAPTER_INDEX"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_read)
        val book = intent.getSerializableExtra(DATA_BOOK) as Book
        chapterContent.layoutManager = LinearLayoutManager(this)
        chapterContent.adapter = ChapterContentAdapter(book.chapterList)
        chapterContent.scrollToPosition(intent.getIntExtra(DATA_CHAPTER_INDEX, 0) * 2 + 1)
        chapterList.layoutManager = LinearLayoutManager(this)
        chapterList.adapter = ChapterListAdapter(book)
        drawer_layout.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
            override fun onDrawerOpened(drawerView: View?) {
                val manager = chapterList.layoutManager as LinearLayoutManager
                val position = manager.findFirstVisibleItemPosition()
                chapterList.scrollToPosition(position)
            }

        })
    }

    override fun onDestroy() {
        super.onDestroy()
        val adapter = chapterContent.adapter as ChapterContentAdapter
        adapter.cancel()
    }

    private class ChapterContentAdapter(val chapters: List<Chapter>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        private val fiction: FictionDataRepository = FictionDataRepositoryImpl()
        private var compDisposable: CompositeDisposable = CompositeDisposable()
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return object : RecyclerView.ViewHolder(with(parent.context) {
                textView {
                    setPadding(16.toDpx(), 8.toDpx(), 16.toDpx(), 8.toDpx())
                    textSize = 16f
                }
            }) {}
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val textView = holder.itemView as TextView
            val chapter = chapters[position / 2]
            if (position % 2 == 0) {
                textView.text = chapter.chapterName
            } else if (chapter.content != null) {
                textView.text = Html.fromHtml(chapter.content)
            } else {
                compDisposable.add(fiction.loadBookChapter(chapter).subscribe({ notifyDataSetChanged() }, {
                    chapter.content = "章节加载失败：${it.message}"
                    notifyDataSetChanged()
                }))
            }
        }

        override fun getItemCount(): Int = chapters.size * 2
        fun cancel() {
            compDisposable.dispose()
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
                chapterContent.scrollToPosition(position * 2)
            }
        }

    }
}

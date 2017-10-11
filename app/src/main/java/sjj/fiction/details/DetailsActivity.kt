package sjj.fiction.details

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.ViewHolder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_details.*
import org.jetbrains.anko.*
import sjj.alog.Log
import sjj.fiction.BaseActivity
import sjj.fiction.R
import sjj.fiction.model.Book
import sjj.fiction.model.Chapter
import sjj.fiction.read.ReadActivity
import sjj.fiction.util.cardView
import sjj.fiction.util.textView
import sjj.fiction.util.toDpx

/**
 * Created by SJJ on 2017/10/10.
 */
class DetailsActivity : BaseActivity() {
    companion object {
        val data = "data"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)
        val book = intent.getSerializableExtra(data) as Book
        bookName.text = book.name
        author.text = book.author
        latestChapter.text = book.latestChapter.chapterName
        latestChapter.setOnClickListener {
            val intent = Intent(it.context, ReadActivity::class.java)
            intent.putExtra(ReadActivity.DATA_BOOK, book)
            intent.putExtra(ReadActivity.DATA_CHAPTER_INDEX, book.chapterList.size - 1)
            it.context.startActivity(intent)
        }
        intro.text = book.intro
        chapterList.layoutManager = LinearLayoutManager(this)
        chapterList.adapter = ChapterListAdapter(book)
        chapterListButton.setOnClickListener {
            chapterList.visibility = if (chapterList.visibility == View.GONE) View.VISIBLE else View.GONE
        }
    }

    private class ChapterListAdapter(val book: Book) : RecyclerView.Adapter<ViewHolder>() {
        val data = book.chapterList
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return object : ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_text_text, parent, false)) {}
        }

        override fun getItemCount() = data.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.itemView.find<TextView>(R.id.text1).text = data[position].chapterName
            holder.itemView.setOnClickListener {
                val intent = Intent(it.context, ReadActivity::class.java)
                intent.putExtra(ReadActivity.DATA_BOOK, book)
                intent.putExtra(ReadActivity.DATA_CHAPTER_INDEX, position)
                it.context.startActivity(intent)
            }
        }

    }
}
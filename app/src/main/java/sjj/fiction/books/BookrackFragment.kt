package sjj.fiction.books

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_books.*
import org.jetbrains.anko.find
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.indeterminateProgressDialog
import sjj.alog.Log
import sjj.fiction.BaseFragment
import sjj.fiction.R
import sjj.fiction.details.DetailsActivity
import sjj.fiction.model.BookGroup
import sjj.fiction.util.domain
import sjj.fiction.util.fictionDataRepository

/**
 * Created by SJJ on 2017/10/7.
 */
class BookrackFragment : BaseFragment() {
    val data: MutableList<BookGroup> = mutableListOf()
    private var compDisposable: CompositeDisposable = CompositeDisposable()
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_books, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bookList.layoutManager = LinearLayoutManager(context)
        bookList.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                return object : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_text_text, parent, false)) {}
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val bookGroup = data[position]
                holder.itemView.find<TextView>(R.id.text1).text = bookGroup.bookName
                holder.itemView.find<TextView>(R.id.text2).text = bookGroup.author
                holder.itemView.setOnClickListener { v ->
                    startActivity(v.context, bookGroup)
                }
            }

            override fun getItemCount(): Int = data.size
            fun startActivity(context: Context, book: BookGroup) {
                val dialog = indeterminateProgressDialog("请稍候")
                compDisposable.add(fictionDataRepository.loadBookDetailsAndChapter(book)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            dialog.dismiss()
                            val intent = Intent(context, DetailsActivity::class.java);
                            intent.putExtra(DetailsActivity.data_book_name, it.bookName)
                            intent.putExtra(DetailsActivity.data_book_author, it.author)
                            startActivity(intent)
                        }, {
                            dialog.dismiss()
                            Log.e("error", it)
                        }))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        fictionDataRepository.loadBookGroups()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    data.clear()
                    data.addAll(it)
                    bookList.adapter.notifyDataSetChanged()
                }, {})
    }

    override fun onStop() {
        super.onStop()
        compDisposable.clear()
    }
}
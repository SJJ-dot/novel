package sjj.novel.view.fragment


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_chapter_list.*
import sjj.alog.Log
import sjj.novel.BaseFragment
import sjj.novel.R
import sjj.novel.databinding.FragmentChapterListBinding
import sjj.novel.databinding.ItemTextTextBinding
import sjj.novel.model.Chapter
import sjj.novel.util.getModel
import sjj.novel.util.getModelActivity
import sjj.novel.util.id
import sjj.novel.util.observeOnMain
import sjj.novel.view.adapter.BaseAdapter


/**
 *展示章节列表
 */
class ChapterListFragment : BaseFragment() {

    companion object {
        val BOOK_NAME = "BOOK_NAME"
        val BOOK_AUTHOR = "BOOK_AUTHOR"
        fun create(bookName: String, bookAuthor: String): ChapterListFragment {
            return ChapterListFragment().apply {
                val bundle = Bundle()
                bundle.putString(BOOK_NAME, bookName)
                bundle.putString(BOOK_AUTHOR, bookAuthor)
                arguments = bundle
            }
        }
    }

    var listener: ItemClickListener? = null

    private lateinit var model: ChapterListViewModel

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        listener = findImpl()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_chapter_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = ChapterListAdapter()
        chapterList.adapter = adapter
        model = getModelActivity { arrayOf(arguments!!.getString(BOOK_NAME), arguments!!.getString(BOOK_AUTHOR)) }
        model.fillViewModel()
                .observeOnMain()
                .subscribe({
                    adapter.data = it
                    adapter.notifyDataSetChanged()
                    model.scrollToReadIndex.set(true)
                }, {
                    showSnackbar(chapterList, "章节列表加载失败:${it.message}")
                }).destroy()
        val bind = DataBindingUtil.bind<FragmentChapterListBinding>(view)
        bind!!.model = model
    }

    private inner class ChapterListAdapter : BaseAdapter() {
        init {
            setHasStableIds(true)
        }
        var data = listOf<ChapterListViewModel.ChapterViewModel>()

        override fun getItemCount(): Int = data.size

        override fun itemLayoutRes(viewType: Int): Int {
            return  R.layout.item_text_text
        }

        override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
            val c = data[position]
            val bind = DataBindingUtil.bind<ItemTextTextBinding>(holder.itemView)
            bind?.model = c
            holder.itemView.setOnClickListener {
                listener?.onClick(c.chapter)
            }
        }

        override fun getItemId(position: Int): Long {
            return data[position].id
        }
    }

    interface ItemClickListener {
        fun onClick(chapter: Chapter)
    }

}

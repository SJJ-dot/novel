package sjj.novel.source

import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_novel_source.*
import kotlinx.android.synthetic.main.item_book_source.view.*
import sjj.alog.Log
import sjj.novel.BaseFragment
import sjj.novel.R
import sjj.novel.data.source.remote.rule.*
import sjj.novel.util.getModel
import sjj.novel.util.gson

class NovelSourceFragment : BaseFragment() {
    private val model by lazy { getModel<NovelSourceViewModel>() }

    private val adapter by lazy { Adapter() }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_novel_source, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        model.saveBookParseRule(BookParseRule().apply {

            sourceName = "笔趣阁"

            topLevelDomain = "yunlaige.com"
            baseUrl = "http://www.yunlaige.com/"
            searchRule = SearchRule().apply {
                charset = Charset.GBK
                method = Method.POST
                serverUrl = "http://www.yunlaige.com/modules/article/search.php"
                searchKey = "searchkey"
                resultRules = listOf(SearchResultRule().apply {
                    bookInfos = ".chart-dashed-list > *"
                    name = "> :nth-child(2) > :nth-child(1) > :nth-child(1) a[href]"
                    author = "> :nth-child(2) > :nth-child(2)"
                    authorRegex = "(.*)/.*"
                    //书籍的名字是一个超链接
                    bookUrl = name
                }, SearchResultRule().apply {
                    bookInfos = ".book-info .info"
                    name = "> :nth-child(1) > :nth-child(1)"
                    author = "> :nth-child(2) > :nth-child(1)"
                })
            }
            introRule = BookIntroRule().apply {
                bookInfo = ".book-info"
                bookName = ".info > :nth-child(1) > :nth-child(1)"
                bookAuthor = ".info > :nth-child(2) > :nth-child(1)"
                bookCoverImgUrl = "> :nth-child(1) > :nth-child(1)"
                bookIntro = ".info > :nth-child(3)"
                bookChapterListUrl = ".info > :nth-child(4) a[href]"
            }

            chapterListRule = BookChapterListRule().apply {
                bookChapterList = "#contenttable > :nth-child(1) a[href]"
                bookChapterUrl = "a"
                bookChapterName = "a"
            }

            chapterContentRule = ChapterContentRule().apply {
                bookChapterContent="#content"
            }

        }).subscribe {
            Log.e(gson.toJson(it))
        }.destroy()


        novel_source.adapter = adapter
        model.getAllBookParseRule()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    adapter.data = it
                    adapter.notifyDataSetChanged()
                }.destroy()
    }

    inner class Adapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var data: List<BookParseRule> = listOf()
        override fun onCreateViewHolder(parent: ViewGroup, p1: Int): RecyclerView.ViewHolder {
            return object : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_book_source, parent, false)) {
            }
        }

        override fun getItemCount(): Int = data.size

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, p1: Int) {
            val rule = data[p1]
            holder.itemView.cb_book_source.text = rule.sourceName
            holder.itemView.cb_book_source.isChecked = rule.enable
            holder.itemView.iv_del_source.setOnClickListener {
                model.deleteBookParseRule(rule)
                        .subscribe()
                        .destroy()
            }

        }
    }
}

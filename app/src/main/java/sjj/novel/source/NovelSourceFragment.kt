package sjj.novel.source

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.widget.RecyclerView
import android.view.*
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_novel_source.*
import kotlinx.android.synthetic.main.item_book_source.view.*
import org.jetbrains.anko.support.v4.startActivity
import sjj.alog.Log
import sjj.novel.BaseFragment
import sjj.novel.R
import sjj.novel.data.source.remote.rule.BookParseRule
import sjj.novel.util.getModel
import sjj.novel.util.gson

class NovelSourceFragment : BaseFragment() {
    private lateinit var model: NovelSourceViewModel

    private val adapter by lazy { Adapter() }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_novel_source, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        novel_source.adapter = adapter
        model = getModel()
        model.getAllBookParseRule()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    it.forEach {
                        if (it.enable)
                            Log.i(gson.toJson(it))
                    }
                    adapter.data = it
                    adapter.notifyDataSetChanged()
                }.destroy()

        setHasOptionsMenu(true)

    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.fragment_novel_source_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.menu_create_novel_source -> {
                startActivity<EditNovelSourceActivity>()
                true
            }
            R.id.menu_load_default_novel_source -> {
                //同步书源。退出后就会停止同步。体验可能不是很好但我并不关心
                newSnackbar(novel_source, "正在同步书源，请稍后……", Snackbar.LENGTH_INDEFINITE)
                model.syncNovelSource()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            newSnackbar(novel_source, "同步成功", Snackbar.LENGTH_SHORT)
                        }, {
                            newSnackbar(novel_source, "同步失败：${it.message}", Snackbar.LENGTH_SHORT)
                        })
                        .destroy("sync novel source")
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
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
            holder.itemView.iv_share_source.setOnClickListener {
                model.share(rule).observeOn(AndroidSchedulers.mainThread()).subscribe({
                    showSnackbar(holder.itemView, "分享成功")
                }, {
                    showSnackbar(holder.itemView, "分享失败:${it.message}")
                }).destroy("share novel source rule")
            }
            holder.itemView.iv_del_source.setOnClickListener {
                model.deleteBookParseRule(rule)
                        .subscribe()
                        .destroy("delete novel source rule")
            }
            holder.itemView.iv_edit_source.setOnClickListener {
                startActivity<EditNovelSourceActivity>(EditNovelSourceActivity.NOVEL_SOURCE_TOP_LEVEL_DOMAIN to rule.topLevelDomain)
            }
            holder.itemView.cb_book_source.setOnCheckedChangeListener { buttonView, isChecked ->
                if (rule.enable != isChecked) {
                    rule.enable = isChecked
                    model.saveBookParseRule(rule)
                            .subscribe()
                }
            }

            holder.itemView.cb_book_source.isChecked = rule.enable

        }
    }
}

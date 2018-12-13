package sjj.novel.source

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_novel_test.*
import kotlinx.android.synthetic.main.item_log_text.view.*
import org.jetbrains.anko.toast
import sjj.alog.Log
import sjj.novel.R
import sjj.novel.logcat.LogCatIBinder
import sjj.novel.logcat.LogCatIBinderCallBack
import sjj.novel.logcat.LogCatService
import sjj.novel.util.getModel
import java.util.*

class NovelTestActivity : AppCompatActivity() {

    companion object {
        const val NOVEL_SOURCE_TOP_LEVEL_DOMAIN = "NOVEL_SOURCE_TOP_LEVEL_DOMAIN"
    }

    private val adapter by lazy { Adapter() }

    private val model by lazy {
        getModel<NovelTestViewModel>(object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return modelClass.getConstructor(String::class.java).newInstance(intent.getStringExtra(NOVEL_SOURCE_TOP_LEVEL_DOMAIN))
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_novel_test)
        console.adapter = adapter
        bindService(Intent(this, LogCatService::class.java), connection, Context.BIND_AUTO_CREATE)

        search.setOnClickListener { _ ->
            model.search(search_input.text.toString().trim()).observeOn(AndroidSchedulers.mainThread()).subscribe { list ->
                if (list.isEmpty()) {
                    toast("搜索结果为空")
                    return@subscribe
                }
                refresh.setOnClickListener { _ ->
                    model.getBook(list.first().url).observeOn(AndroidSchedulers.mainThread()).subscribe { book ->
                        if (book.chapterList.isEmpty()) {
                            toast("小说章节列表为空")
                        } else {
                            chapter.setOnClickListener {_->
                                model.getChapterContent(book.chapterList.first()).observeOn(AndroidSchedulers.mainThread()).subscribe{
                                    if (it.content.isNullOrBlank()) {
                                        toast("小说章节内容为空")
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }
        clear.setOnClickListener {
            adapter.data.clear()
            adapter.notifyDataSetChanged()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(connection)
    }

    private val connection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            toast("logcat 服务已断开")
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            toast("logcat 服务已连接")
            service?.also {
                val logcat = it as LogCatIBinder
                logcat.register(object : LogCatIBinderCallBack.Stub() {
                    override fun onCapture(msg: String?) {
                        runOnUiThread {
                            adapter.data.addFirst(msg)
                            if (adapter.data.size > 20) {
                                adapter.data.removeLast()
                            }
                            adapter.notifyDataSetChanged()
                        }
                    }
                })
            }
        }
    }

    class Adapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        val data = LinkedList<String>()

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
            return object : RecyclerView.ViewHolder(LayoutInflater.from(p0.context).inflate(R.layout.item_log_text, p0, false)) {
            }
        }

        override fun getItemCount(): Int = data.size

        override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {
            p0.itemView.text.text = data[p1]
        }
    }

}

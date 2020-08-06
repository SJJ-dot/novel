package sjj.novel.view.module.source

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.ViewGroup
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_novel_test.*
import kotlinx.android.synthetic.main.item_log_text.view.*
import org.jetbrains.anko.toast
import sjj.alog.Log
import sjj.novel.BaseActivity
import sjj.novel.R
import sjj.novel.logcat.LogCatIBinder
import sjj.novel.logcat.LogCatIBinderCallBack
import sjj.novel.logcat.LogCatService
import sjj.novel.util.getModel
import sjj.novel.util.observeOnMain
import java.util.*

class NovelTestActivity : BaseActivity() {

    companion object {
        const val NOVEL_SOURCE_TOP_LEVEL_DOMAIN = "NOVEL_SOURCE_TOP_LEVEL_DOMAIN"
    }

    private val adapter by lazy { Adapter() }

    private lateinit var model: NovelTestViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_novel_test)
        console.adapter = adapter
        bindService(Intent(this, LogCatService::class.java), connection, Context.BIND_AUTO_CREATE)
        model = getModel {
            arrayOf(intent.getStringExtra(NOVEL_SOURCE_TOP_LEVEL_DOMAIN) ?: "")
        }
        search.setOnClickListener { _ ->
            model.search(search_name.text.toString().trim()).observeOnMain().subscribe { list ->
                if (list.isEmpty()) {
                    toast("搜索结果为空")
                    return@subscribe
                }
                refresh.setOnClickListener { _ ->
                    model.getBook(list.first().url).observeOnMain().subscribe { book ->
                        if (book.chapterList.isEmpty()) {
                            toast("小说章节列表为空")
                        } else {
                            chapter.setOnClickListener { _ ->
                                model.getChapterContent(book.chapterList.first()).observeOnMain().subscribe {
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
            //服务已连接回调是在主线程
            Log.e("LogCatService 服务已断开")
            toast("LogCatService 服务已断开")
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            //服务已连接回调是在主线程

            toast("LogCatService 服务已连接")
            service?.also {

                val callback = object : LogCatIBinderCallBack.Stub() {
                    override fun onCapture(msg: String?) {
                        //回调不在主线程
                        runOnUiThread {
                            adapter.data.addFirst(msg)
                            if (adapter.data.size > 2000) {
                                adapter.data.removeLast()
                            }
                            adapter.notifyDataSetChanged()
                        }
                    }
                }
                val logcat =LogCatIBinder.Stub.asInterface(it)
                Log.e("LogCatService 服务已连接 binderProxy:$service binder:$logcat callback:$callback ")
                logcat.register(callback)
            }
        }
    }

    class Adapter : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {

        val data = LinkedList<String>()

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
            return object : androidx.recyclerview.widget.RecyclerView.ViewHolder(LayoutInflater.from(p0.context).inflate(R.layout.item_log_text, p0, false)) {
            }
        }

        override fun getItemCount(): Int = data.size

        override fun onBindViewHolder(p0: androidx.recyclerview.widget.RecyclerView.ViewHolder, p1: Int) {
            p0.itemView.text.text = data[p1]
        }
    }

}

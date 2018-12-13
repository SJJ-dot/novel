package sjj.novel.source

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
import kotlinx.android.synthetic.main.activity_novel_test.*
import kotlinx.android.synthetic.main.item_log_text.view.*
import org.jetbrains.anko.toast
import sjj.alog.Log
import sjj.novel.R
import sjj.novel.logcat.LogCatIBinder
import sjj.novel.logcat.LogCatIBinderCallBack
import sjj.novel.logcat.LogCatService
import java.util.*

class NovelTestActivity : AppCompatActivity() {
    private val adapter by lazy { Adapter() }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_novel_test)
        console.adapter = adapter
        bindService(Intent(this, LogCatService::class.java), connection, Context.BIND_AUTO_CREATE)
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

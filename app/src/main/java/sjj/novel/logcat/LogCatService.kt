package sjj.novel.logcat

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.Process
import android.os.RemoteCallbackList
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.StringReader
import java.lang.Exception
import java.util.concurrent.BlockingDeque
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue


class LogCatService : Service() {
    private val mCallbacks = RemoteCallbackList<LogCatIBinderCallBack>()
    /**
     * logcat 消息队列
     */
    private val blockingQueue = LinkedBlockingQueue<String>()
    @Volatile
    private var isRunning = false

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    private val binder: LogCatIBinder.Stub = object : LogCatIBinder.Stub() {
        override fun register(callback: LogCatIBinderCallBack?) {
            mCallbacks.register(callback)
            Log.e("LogCatService", "register")
        }

        override fun unRegister(callback: LogCatIBinderCallBack?) {
            mCallbacks.unregister(callback)
            Log.e("LogCatService", "unRegister")
        }
    }

    private fun sendMsg(msg: String) {
        val broadcast = mCallbacks.beginBroadcast()
        for (i in 0 until broadcast) {
            mCallbacks.getBroadcastItem(i).onCapture(msg)
        }
        mCallbacks.finishBroadcast()
    }

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        Thread(readLogCat).start()
        Thread(sendLogCat).start()
        Log.e("LogCatService", "onCreate")
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        Log.e("LogCatService", "onDestroy")
    }

    private val readLogCat: () -> Unit = {
        Log.e("LogCatService", "readLogCat")
        while (isRunning) {
            try {
                val exec = Runtime.getRuntime().exec("logcat")
                val logcatInput = exec.inputStream
                val reader = BufferedReader(InputStreamReader(logcatInput))
                while (isRunning) {
                    val line = reader.readLine()
                    if (line!=null)
                        blockingQueue.offer(line)
                }
            } catch (e: Exception) {
                blockingQueue.offer(Log.getStackTraceString(e))
                Log.e("LogCatService", "readLogCat ", e)
            }
        }
    }

    private val sendLogCat: () -> Unit = {
        while (isRunning) {
            try {
                val poll = blockingQueue.poll()
                if (poll != null)
                    sendMsg(poll)
            } catch (e: Exception) {
                blockingQueue.offer(Log.getStackTraceString(e))
                Log.e("LogCatService", "sendLogCat ", e)
            }
        }
    }

}
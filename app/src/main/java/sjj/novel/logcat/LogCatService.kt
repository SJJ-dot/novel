package sjj.novel.logcat

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.RemoteCallbackList
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Exception
import java.util.concurrent.LinkedBlockingQueue


class LogCatService : Service() {
    private val mCallbacks = RemoteCallbackList<LogCatIBinderCallBack>()
    /**
     * logcat 消息队列
     */
    private val blockingQueue = LinkedBlockingQueue<String>()
    @Volatile
    private var isRunning = false
    private var readLogThread: Thread? = null
    private var sendLogThread: Thread? = null

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    private val binder: LogCatIBinder.Stub = object : LogCatIBinder.Stub() {
        override fun register(callback: LogCatIBinderCallBack?) {
            mCallbacks.register(callback)
            Log.e("LogCatService", "register binder:$this callback:$callback")
        }

        override fun unRegister(callback: LogCatIBinderCallBack?) {
            mCallbacks.unregister(callback)
            Log.e("LogCatService", "unRegister binder:$this callback:$callback")
        }
    }

    private fun sendMsg(msg: String) {
        val broadcast = mCallbacks.beginBroadcast()
        for (i in 0 until broadcast) {
            try {
                mCallbacks.getBroadcastItem(i).onCapture(msg)
            } catch (e: Exception) {
                Log.e("LogCatService","sendMsg exception LogCatIBinderCallBack",e)
            }
        }
        mCallbacks.finishBroadcast()
    }

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        readLogThread = Thread(readLogCat)
        readLogThread?.start()
        sendLogThread = Thread(sendLogCat)
        sendLogThread?.start()
        Log.e("LogCatService", "onCreate")
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        readLogThread?.interrupt()
        sendLogThread?.interrupt()
        Log.e("LogCatService", "onDestroy")
    }

    private val readLogCat: () -> Unit = {
        Log.e("LogCatService", "readLogCat")
        while (isRunning) {
            var reader:BufferedReader?=null
            try {
                val exec = Runtime.getRuntime().exec("logcat")
                val logcatInput = exec.inputStream
                reader = BufferedReader(InputStreamReader(logcatInput))
                while (isRunning) {
                    val line = reader.readLine()
                    if (line != null)
                        blockingQueue.offer(line)
                }
            } catch (e: Exception) {
                Log.e("LogCatService", "readLogCat ", e)
            }finally {
                try {
                    reader?.close()
                } catch (e: Exception) {

                }
            }
        }
    }

    private val sendLogCat: () -> Unit = {
        while (isRunning) {
            try {
                val poll = blockingQueue.take()
                if (poll != null)
                    sendMsg(poll)
            } catch (e: Exception) {
                Log.e("LogCatService", "sendLogCat ", e)
            }
        }
    }

}
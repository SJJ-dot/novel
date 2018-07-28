package sjj.fiction

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_crash.*
import sjj.alog.Log
import sjj.fiction.util.stackTraceString

class CrashActivity : AppCompatActivity() {
    companion object {
        val CRASH_DATA = "CRASH_DATA"
        val THREAD_INFO = "THREAD_INFO"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crash)
        setSupportActionBar(toolbar)
        val supportActionBar = supportActionBar!!
        supportActionBar.title = intent.getStringExtra(THREAD_INFO)
        val stackTraceString = intent.getStringExtra(CRASH_DATA)
        contentText.text = stackTraceString
        Log.e(stackTraceString)
    }

    override fun onStop() {
        super.onStop()
        System.exit(0)
    }
}

package sjj.novel

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.appbar_layout.*
import me.imid.swipebacklayout.lib.SwipeBackLayout
import me.imid.swipebacklayout.lib.app.SwipeBackActivity
import sjj.rx.AutoDisposeEnhance


/**
 * Created by SJJ on 2017/10/5.
 */
abstract class BaseActivity : SwipeBackActivity(), AutoDisposeEnhance {
    private var snackbar: Snackbar? = null
    protected open val isEnableSwipeBack = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //滑动返回设置
        swipeBackLayout?.setEnableGesture(isEnableSwipeBack)
        swipeBackLayout?.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT)
    }

    @SuppressLint("WrongConstant")
    fun showSnackbar(view: View, msg: String, @Snackbar.Duration duration: Int = Snackbar.LENGTH_SHORT) {
        if (snackbar == null) {
            snackbar = Snackbar.make(view, msg, duration)
        } else {
            snackbar?.setText(msg)
            snackbar?.duration = duration
        }
        snackbar?.show()
    }

    fun newSnackbar(view: View, msg: String, duration: Int = Snackbar.LENGTH_SHORT) {
        snackbar = Snackbar.make(view, msg, duration)
        snackbar?.show()
    }

    fun dismissSnackbar() {
        snackbar?.dismiss()
        snackbar = null
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        toolbar?.also {
            setSupportActionBar(it)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> false
        }
    }

    override fun getSharedPreferences(name: String?, mode: Int): SharedPreferences {
        return application.getSharedPreferences(name, mode)
    }

}
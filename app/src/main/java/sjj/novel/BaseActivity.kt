package sjj.novel

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.appbar_layout.*
import sjj.rx.AutoDisposeEnhance


/**
 * Created by SJJ on 2017/10/5.
 */
abstract class BaseActivity : AppCompatActivity(), AutoDisposeEnhance {
    private var snackbar: Snackbar? = null

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
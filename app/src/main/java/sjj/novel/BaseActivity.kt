package sjj.novel

import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.View
import io.reactivex.disposables.Disposable
import sjj.novel.util.destroy
import sjj.novel.util.pause
import sjj.novel.util.stop


/**
 * Created by SJJ on 2017/10/5.
 */
abstract class BaseActivity : AppCompatActivity() {
    fun Disposable.destroy(onceKey: String? = null) {
        destroy(onceKey, lifecycle)
    }

    fun Disposable.stop(onceKey: String? = null) {
        stop(onceKey, lifecycle)
    }

    fun Disposable.pause(onceKey: String? = null) {
        pause(onceKey, lifecycle)
    }


    private var snackbar: Snackbar? = null

    fun showSnackbar(view: View, msg: String, duration: Int = Snackbar.LENGTH_SHORT) {
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


}
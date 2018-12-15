package sjj.novel

import android.support.design.widget.Snackbar
import android.support.v4.app.DialogFragment
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import io.reactivex.disposables.Disposable
import sjj.novel.util.destroy
import sjj.novel.util.pause
import sjj.novel.util.stop

/**
 * Created by SJJ on 2017/10/7.
 */
open class BaseFragment : DialogFragment() {
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

    override fun onStart() {
        super.onStart()
        dialog?.also {
            val dm = DisplayMetrics();
            activity!!.windowManager.defaultDisplay.getMetrics(dm);
            it.window?.setLayout(dm.widthPixels, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

}
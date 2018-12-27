package sjj.novel

import com.google.android.material.snackbar.Snackbar
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import sjj.alog.Log
import sjj.rx.AutoDisposeEnhance

/**
 * Created by SJJ on 2017/10/7.
 */
open class BaseFragment : androidx.fragment.app.DialogFragment(), AutoDisposeEnhance {

    private var snackbar: Snackbar? = null

    fun showSnackbar(view: View?, msg: String, duration: Int = Snackbar.LENGTH_SHORT) {
        Log.i(msg)
        if (snackbar == null) {
            snackbar = Snackbar.make(view ?: return, msg, duration)
        } else {
            snackbar?.setText(msg)
            snackbar?.duration = duration
        }
        snackbar?.show()
    }

    fun newSnackbar(view: View?, msg: String, duration: Int = Snackbar.LENGTH_SHORT) {
        Log.i(msg)
        snackbar = Snackbar.make(view ?: return, msg, duration)
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

    fun show(manager: androidx.fragment.app.FragmentManager?) {
        super.show(manager, null)
    }

    fun show(transaction: androidx.fragment.app.FragmentTransaction?): Int {
        return super.show(transaction, javaClass.name)
    }

}
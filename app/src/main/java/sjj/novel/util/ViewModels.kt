package sjj.novel.util

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity

inline fun <reified T : ViewModel> Fragment.getModel(crossinline args: () -> Array<out Any> = { arrayOf() }): T {
    return ViewModelProviders.of(this, object : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return modelClass.getConstructor(*args().map { it.javaClass }.toTypedArray()).newInstance(*args())
        }
    }).get(T::class.java)
}

inline fun <reified T : ViewModel> Fragment.getModelActivity(crossinline args: () -> Array<out Any> = { arrayOf() }): T {
    return ViewModelProviders.of(activity!!, object : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return modelClass.getConstructor(*args().map { it.javaClass }.toTypedArray()).newInstance(*args())
        }
    }).get(T::class.java)
}

inline fun <reified T : ViewModel> FragmentActivity.getModel(crossinline args: () -> Array<out Any> = { arrayOf() }): T {
    return ViewModelProviders.of(this, object : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return modelClass.getConstructor(*args().map { it.javaClass }.toTypedArray()).newInstance(*args())
        }
    }).get(T::class.java)
}
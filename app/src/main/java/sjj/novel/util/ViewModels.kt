package sjj.novel.util

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity

inline fun <reified T : ViewModel> Fragment.getModel(factory: ViewModelProvider.Factory? = null): T {
    return ViewModelProviders.of(activity!!, factory).get(T::class.java)
}

inline fun <reified T : ViewModel> FragmentActivity.getModel(factory: ViewModelProvider.Factory? = null): T {
    return ViewModelProviders.of(this, factory).get(T::class.java)
}

inline fun <reified T : ViewModel> Fragment.lazyModel(crossinline args: () -> Array<out Any> = { arrayOf() }) = lazy {
    getModel<T>(object : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return modelClass.getConstructor(*args().map { it.javaClass }.toTypedArray()).newInstance(*args())
        }
    })
}

inline fun <reified T : ViewModel> FragmentActivity.lazyModel(crossinline args: () -> Array<out Any> = { arrayOf() }) = lazy {
    getModel<T>(object : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return modelClass.getConstructor(*args().map { it.javaClass }.toTypedArray()).newInstance(*args())
        }
    })
}
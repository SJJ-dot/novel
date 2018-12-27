package sjj.novel.util

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

inline fun <reified T : ViewModel> androidx.fragment.app.Fragment.getModel(crossinline args: () -> Array<out Any> = { arrayOf() }): T {
    return ViewModelProviders.of(this, object : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return modelClass.getConstructor(*args().map { it.javaClass }.toTypedArray()).newInstance(*args())
        }
    }).get(T::class.java)
}

inline fun <reified T : ViewModel> androidx.fragment.app.Fragment.getModelActivity(crossinline args: () -> Array<out Any> = { arrayOf() }): T {
    return ViewModelProviders.of(activity!!, object : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return modelClass.getConstructor(*args().map { it.javaClass }.toTypedArray()).newInstance(*args())
        }
    }).get(T::class.java)
}

inline fun <reified T : ViewModel> androidx.fragment.app.FragmentActivity.getModel(crossinline args: () -> Array<out Any> = { arrayOf() }): T {
    return ViewModelProviders.of(this, object : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return modelClass.getConstructor(*args().map { it.javaClass }.toTypedArray()).newInstance(*args())
        }
    }).get(T::class.java)
}
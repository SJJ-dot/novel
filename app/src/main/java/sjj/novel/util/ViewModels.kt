package sjj.novel.util

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

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

open class ViewModelDispose : ViewModel() {
    private val disposes = CompositeDisposable()
    private val disposesMap = mutableMapOf<String,Disposable>()
    fun Disposable.autoDispose(key: String? =null) {
        if (key == null) {
            disposes.add(this)
        } else {
            synchronized(disposesMap) {
                disposesMap.put(key,this)
            }
        }
    }

    fun removeDispose(key: String) {
        synchronized(disposesMap) {
            disposesMap.remove(key)?.dispose()
        }
    }

    override fun onCleared() {
        disposes.clear()
        synchronized(disposesMap) {
            disposesMap.values.forEach {
                it.dispose()
            }
            disposesMap.clear()
        }
    }
}

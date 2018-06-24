package sjj.fiction.util

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import sjj.alog.Log
import java.util.*
import kotlin.collections.HashMap

private val map = Collections.synchronizedMap(HashMap<String, BaseLifecycleObserver>())

fun Disposable.destroy(onceKey: String? = null, lifecycle: Lifecycle) {
    lifecycle.addObserver(BaseLifecycleObserver(this, lifecycle, onceKey, Lifecycle.Event.ON_DESTROY))
}

fun Disposable.stop(onceKey: String? = null, lifecycle: Lifecycle) {
    lifecycle.addObserver(BaseLifecycleObserver(this, lifecycle, onceKey, Lifecycle.Event.ON_STOP))
}

fun Disposable.pause(onceKey: String? = null, lifecycle: Lifecycle) {
    lifecycle.addObserver(BaseLifecycleObserver(this, lifecycle, onceKey, Lifecycle.Event.ON_PAUSE))
}

class BaseLifecycleObserver(val disposable: Disposable,
                            private val lifecycle: Lifecycle,
                            private val onceKey: String? = null,
                            private val event: Lifecycle.Event = Lifecycle.Event.ON_DESTROY) : LifecycleObserver {
    init {
        if (onceKey != null) {
            val observer = map.remove(onceKey)
            map[onceKey] = this
            if (observer != null) {
                observer.disposable.dispose()
                lifecycle.removeObserver(observer)
            }
        }
        lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        if (event == Lifecycle.Event.ON_STOP) {
            clean()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        if (event == Lifecycle.Event.ON_DESTROY) {
            clean()
        }
    }

    private fun clean() {
        disposable.dispose()
        if (onceKey != null)
            map.remove(onceKey)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        if (disposable.isDisposed) {
            lifecycle.removeObserver(this)
            if (onceKey != null)
                map.remove(onceKey)
        }
        if (event == Lifecycle.Event.ON_PAUSE) {
            clean()
        }
    }

}
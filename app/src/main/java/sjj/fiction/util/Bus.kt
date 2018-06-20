package sjj.fiction.util

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import io.reactivex.disposables.Disposable
import io.reactivex.processors.PublishProcessor
import sjj.fiction.model.Event
import java.util.*

/**
 * Created by SJJ on 2017/11/25.
 */
val processor = PublishProcessor.create<Any>().toSerialized()
private val map = WeakHashMap<String, MLifecycleObserver>() //Collections.synchronizedMap(WeakHashMap<String, MLifecycleObserver>())
fun rxPush(o: Any) {
    processor.onNext(o)
}

inline fun <reified T> rxOfType() = processor.ofType(T::class.java)

fun Disposable.auto(onceKey: String? = null, lifecycle: Lifecycle) {
    val ob = MLifecycleObserver(this, lifecycle)
    if (onceKey != null) {
        val observer = map.remove(onceKey)
        map[onceKey] = ob
        if (observer != null) {
            observer.disposable.dispose()
            lifecycle.removeObserver(observer)
        }
    }
    lifecycle.addObserver(ob)
}

class MLifecycleObserver(val disposable: Disposable, private val lifecycle: Lifecycle) : LifecycleObserver {
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        disposable.dispose()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        if (disposable.isDisposed) {
            lifecycle.removeObserver(this)
        }
    }

}
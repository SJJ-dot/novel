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
fun rxPush(o: Any) {
    processor.onNext(o)
}

inline fun <reified T> rxOfType() = processor.ofType(T::class.java)
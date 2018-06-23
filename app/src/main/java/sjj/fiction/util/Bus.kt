package sjj.fiction.util

import io.reactivex.processors.PublishProcessor

/**
 * Created by SJJ on 2017/11/25.
 */
val processor = PublishProcessor.create<Any>().toSerialized()
fun rxPush(o: Any) {
    processor.onNext(o)
}

inline fun <reified T> rxOfType() = processor.ofType(T::class.java)
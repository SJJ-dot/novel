package sjj.fiction.util

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.Callable


/**
 * Created by SJJ on 2017/10/8.
 */
fun <T> def(supplier: () -> T): io.reactivex.Observable<T> {
    return Observable.fromCallable(supplier)
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
}

fun <T> errorObservable(message: String) = def<T> { throw Exception(message) }
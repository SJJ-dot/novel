package sjj.fiction.util

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.Callable


/**
 * Created by SJJ on 2017/10/8.
 */
fun <T> def(scheduler: Scheduler = Schedulers.computation(), supplier: () -> T): io.reactivex.Observable<T> {
    return Observable.fromCallable(supplier)
            .subscribeOn(scheduler)
            .observeOn(AndroidSchedulers.mainThread())
}

fun <T> errorObservable(message: String) = def<T> { throw Exception(message) }
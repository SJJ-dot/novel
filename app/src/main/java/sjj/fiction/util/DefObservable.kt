package sjj.fiction.util

import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers


/**
 * Created by SJJ on 2017/10/8.
 */
fun <T> def(scheduler: Scheduler = Schedulers.computation(), supplier: () -> T): io.reactivex.Observable<T> {
    return Observable.fromCallable(supplier)
            .subscribeOn(scheduler)
}

fun <T> observableCreate(run: (ObservableEmitter<T>) -> Unit): Observable<T> = Observable.create<T>(run)
        .subscribeOn(Schedulers.computation())
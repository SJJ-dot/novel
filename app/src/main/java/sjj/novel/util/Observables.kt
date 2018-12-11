package sjj.novel.util

import io.reactivex.Observable
import io.reactivex.ObservableSource

fun <T, R> Observable<out List<T>>.lazyFromIterable(mapper: (T) -> Observable<R>): Observable<Observable<R>> {
    return flatMap { list ->
        Observable.create<Observable<R>> { emitter ->
            fun emi(index: Int) {
                if (list.size > index) {
                    emitter.onNext(mapper(list[index]).doOnComplete {
                        emi(index + 1)
                    })
                } else {
                    emitter.onComplete()
                }
            }
            emi(0)
        }
    }
}

fun <T> ObservableSource<out ObservableSource<out T>>.concat(): Observable<T> {
    return Observable.concat(this)
}

fun <T> T.toObservable(): Observable<T> {
    return Observable.create {
        if (this != null) {
            it.onNext(this)
        }
        it.onComplete()
        Observable.fromCallable {  }
    }
}

fun <T> fromCallableOrNull(callable:()->T): Observable<T> {
    return Observable.create {
        val t = callable()
        if (t != null) {
            it.onNext(t)
        }
        it.onComplete()
    }
}
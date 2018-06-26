package sjj.fiction.util

import io.reactivex.Observable
import io.reactivex.ObservableSource

fun <T, R> Observable<List<T>>.lazyFromIterable(mapper: (T) -> Observable<R>): Observable<Observable<R>> {
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
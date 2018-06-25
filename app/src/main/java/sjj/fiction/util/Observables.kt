package sjj.fiction.util

import io.reactivex.Observable

fun <T, R> Observable<List<T>>.lazyFromIterable(mapper: (T) -> Observable<R>): Observable<Observable<R>> {
    return flatMap { list ->
        Observable.create<Observable<R>> { emitter ->
            var emi: ((Int) -> Unit)? = null
            emi = { index ->
                if (list.size > index + 1) {
                    emitter.onNext(mapper(list[index]).doOnComplete {
                        emi?.invoke(index + 1)
                    })
                } else {
                    emitter.onComplete()
                }
            }
            emi(0)
        }
    }
}
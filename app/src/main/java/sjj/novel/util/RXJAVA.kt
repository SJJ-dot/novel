package sjj.novel.util

import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

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

fun <T> Observable<T>.observeOnMain() = observeOn(AndroidSchedulers.mainThread(),false,1)
fun <T> Observable<T>.subscribeOnIo() = subscribeOn(Schedulers.io())
fun <T> Flowable<T>.observeOnMain() = observeOn(AndroidSchedulers.mainThread(),false,1)
fun <T> Maybe<T>.observeOnMain() = observeOn(AndroidSchedulers.mainThread())
fun <T> Single<T>.observeOnMain() = observeOn(AndroidSchedulers.mainThread())

fun <T> Observable<T>.subscribeOnSingle() = subscribeOn(Schedulers.single()).observeOn(Schedulers.io())
fun <T> Flowable<T>.subscribeOnSingle() = subscribeOn(Schedulers.single()).observeOn(Schedulers.io())
fun <T> Single<T>.subscribeOnSingle() = subscribeOn(Schedulers.single()).observeOn(Schedulers.io())

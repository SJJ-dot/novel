package sjj.fiction.util

import io.reactivex.schedulers.Schedulers
import io.reactivex.disposables.Disposable
import io.reactivex.Scheduler
import io.reactivex.functions.Consumer
import sjj.alog.Log
import java.util.concurrent.TimeUnit


/**
 * Created by SJJ on 2017/10/7.
 */
fun submit(scheduler: Scheduler, runnable: () -> Unit, error: (t: Exception) -> Unit): Disposable {
    return scheduler.createWorker().schedule {
        try {
            runnable()
        } catch (e: Exception) {
            error(e)
        }
    }
}

fun submit(runnable: () -> Unit, error: (t: Exception) -> Unit): Disposable {
    return submit(Schedulers.computation(), runnable, error)
}

fun submit(scheduler: Scheduler, runnable: () -> Unit): Disposable {
    return submit(scheduler, runnable) { Log.e("pool error", it) }
}

fun submit(runnable: () -> Unit): Disposable {
    return submit(Schedulers.computation(), runnable)
}

fun submit(scheduler: Scheduler, run: () -> Unit, delay: Long, error: (t: Exception) -> Unit): Disposable {
    return scheduler.createWorker().schedule({
        try {
            run()
        } catch (e: Exception) {
            error(e)
        }
    }, delay, TimeUnit.MILLISECONDS)
}

fun submit(runnable: () -> Unit, delay: Long, error: (t: Exception) -> Unit): Disposable {
    return submit(Schedulers.computation(), runnable, delay, error)
}

fun submit(scheduler: Scheduler, runnable: () -> Unit, delay: Long): Disposable {
    return submit(scheduler, runnable, delay) { Log.e("pool error", it) }
}

fun submit(runnable: () -> Unit, delay: Long): Disposable {
    return submit(Schedulers.computation(), runnable, delay)
}

fun submit(scheduler: Scheduler, run: () -> Unit, initialDelay: Long, period: Long, error: (t: Exception) -> Unit): Disposable {
    return scheduler.createWorker().schedulePeriodically({
        try {
            run()
        } catch (e: Exception) {
            error(e)
        }
    }, initialDelay, period, TimeUnit.MILLISECONDS)
}

fun submit(runnable: () -> Unit, initialDelay: Long, period: Long, error: (t: Exception) -> Unit): Disposable {
    return submit(Schedulers.computation(), runnable, initialDelay, period, error)
}

fun submit(scheduler: Scheduler, runnable: () -> Unit, initialDelay: Long, period: Long): Disposable {
    return submit(scheduler, runnable, initialDelay, period){Log.e("pool error", it)}
}

fun submit(runnable: () -> Unit, initialDelay: Long, period: Long): Disposable {
    return submit(Schedulers.computation(), runnable, initialDelay, period)
}
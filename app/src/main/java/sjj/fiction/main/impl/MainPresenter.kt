package sjj.fiction.main.impl

import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import sjj.fiction.data.Repository.FictionDataRepository
import sjj.fiction.main.MainContract
import sjj.fiction.model.BookGroup
import sjj.fiction.util.fictionDataRepository

/**
 * Created by SJJ on 2017/10/7.
 */
class MainPresenter(private val view: MainContract.View) : MainContract.Presenter {
    private val com = CompositeDisposable()
    val fiction: FictionDataRepository = fictionDataRepository

    init {
        view.setPresenter(this)
    }

    override fun start() {
    }

    override fun stop() {
        com.clear()
    }

    override fun showAutoText() {
        fiction.getSearchHistory()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(view::setAutoText, {})
                .also { com.add(it) }
    }

    override fun search(text: String) {
        fiction.search(text).doOnNext {
            fiction.getSearchHistory().flatMap {
                val set = it.toMutableSet()
                set.add(text)
                fiction.setSearchHistory(set.toList())
            }.subscribe({}, {}).also { com.add(it) }
        }.observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<List<BookGroup>> {
                    override fun onSubscribe(d: Disposable) {
                        com.add(d)
                        view.setSearchProgressHint(true)
                    }

                    override fun onError(e: Throwable) {
                        view.setSearchProgressHint(false)
                        view.setSearchErrorHint(e)
                    }

                    override fun onComplete() {
                        view.setSearchProgressHint(false)
                    }

                    override fun onNext(t: List<BookGroup>) {
                        view.setSearchBookList(t)
                    }
                })
    }
}
package sjj.fiction.main.impl

import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import sjj.alog.Log
import sjj.fiction.AppConfig
import sjj.fiction.data.Repository.FictionDataRepository
import sjj.fiction.data.Repository.fictionDataRepository
import sjj.fiction.main.MainContract
import sjj.fiction.model.BookGroup

/**
 * Created by SJJ on 2017/10/7.
 */
class MainPresenter(private val view: MainContract.View) : MainContract.Presenter {
    private val com = CompositeDisposable()
    val fiction = fictionDataRepository

    init {
        view.setPresenter(this)
    }

    override fun start() {
    }

    override fun stop() {
        com.clear()
    }

    override fun search(text: String) {
        fiction.search(text).doOnNext {
            AppConfig.searchHistory = AppConfig.searchHistory.toMutableSet().apply { add(text) }
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
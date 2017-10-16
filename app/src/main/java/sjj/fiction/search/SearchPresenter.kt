package sjj.fiction.search

import android.content.Context
import android.content.Intent
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import sjj.alog.Log
import sjj.fiction.data.Repository.FictionDataRepository
import sjj.fiction.data.source.remote.dhzw.DhzwDataSource
import sjj.fiction.data.source.remote.yunlaige.YunlaigeDataSource
import sjj.fiction.details.DetailsActivity
import sjj.fiction.model.BookGroup
import sjj.fiction.util.errorObservable
import sjj.fiction.util.fictionDataRepository

/**
 * Created by SJJ on 2017/10/8.
 */
class SearchPresenter(private val view: SearchContract.view) : SearchContract.presenter {
    private val sources = arrayOf<FictionDataRepository.RemoteSource>(DhzwDataSource(), YunlaigeDataSource())

    private var data: FictionDataRepository? = null
    override fun start() {
        data = fictionDataRepository
        fictionDataRepository.getSearchHistory().subscribe({
            view.notifyAutoTextChange(it)
        }, {})
    }

    override fun stop() {
        data = null
    }

    override fun search(text: String): Observable<List<BookGroup>> = (data?.search(text) ?: errorObservable("this presenter not start"))
            .observeOn(Schedulers.io())
            .doOnNext {
                val dataRepository = data ?: return@doOnNext
                val lock = java.lang.Object()
                dataRepository.getSearchHistory().flatMap({
                    val set = it.toMutableSet()
                    set.add(text)
                    dataRepository.setSearchHistory(set.toList())
                }).subscribe({
                    view.notifyAutoTextChange(it)
                    synchronized(lock) { lock.notify() }
                }, {
                    synchronized(lock) { lock.notify() }
                })
                synchronized(lock) {
                    lock.wait()
                }
            }.observeOn(AndroidSchedulers.mainThread())

    override fun onSelect(book: BookGroup, context: Context): Observable<BookGroup> = (data?.loadBookDetailsAndChapter(book) ?: errorObservable("this presenter not start"))
            .doOnNext {
                val intent = Intent(context, DetailsActivity::class.java);
                intent.putExtra(DetailsActivity.data, it)
                context.startActivity(intent)
            }
}
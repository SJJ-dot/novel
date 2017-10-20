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
import sjj.fiction.util.observableCreate

/**
 * Created by SJJ on 2017/10/8.
 */
class SearchPresenter(private val view: SearchContract.view) : SearchContract.presenter {

    private var data: FictionDataRepository = fictionDataRepository
    override fun start() {
    }

    override fun stop() {
    }

    override fun search(text: String): Observable<List<BookGroup>> = observableCreate { emitter ->
        data.search(text).subscribe({ listBook ->
            emitter.onNext(listBook)
        }, emitter::onError, {
            data.getSearchHistory().flatMap({
                val set = it.toMutableSet()
                set.add(text)
                data.setSearchHistory(set.toList())
            }).observeOn(AndroidSchedulers.mainThread()).doOnNext {
                view.notifyAutoTextChange(it)
            }.observeOn(Schedulers.computation()).subscribe({ }, {
                emitter.onComplete()
            }, {
                emitter.onComplete()
            })
        })
    }

    override fun onSelect(book: BookGroup, context: Context): Observable<BookGroup> = data.loadBookDetailsAndChapter(book)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext {
                val intent = Intent(context, DetailsActivity::class.java);
                intent.putExtra(DetailsActivity.data_book_name, it.bookName)
                intent.putExtra(DetailsActivity.data_book_author, it.author)
                context.startActivity(intent)
            }
}
package sjj.fiction.search

import android.content.Context
import android.content.Intent
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import sjj.fiction.data.Repository.fictionDataRepository
import sjj.fiction.details.DetailsActivity
import sjj.fiction.model.BookGroup

/**
 * Created by SJJ on 2017/10/8.
 */
class SearchPresenter(private val view: SearchContract.view) : SearchContract.presenter {
    private var data = fictionDataRepository
    private val com = CompositeDisposable()
    init {
        view.setPresenter(this)
    }
    override fun start() {}
    override fun stop() {com.clear()}
    override fun onSelect(book: BookGroup, context: Context){
        data.loadBookDetailsAndChapter(book)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object :Observer<BookGroup> {
                    override fun onError(e: Throwable) {
                        view.setLoadBookDetailsErrorHint(e)
                        view.setLoadBookDetailsHint(false)
                    }

                    override fun onNext(t: BookGroup) {
                        val intent = Intent(context, DetailsActivity::class.java);
                        intent.putExtra(DetailsActivity.data_book_name, t.bookName)
                        intent.putExtra(DetailsActivity.data_book_author, t.author)
                        context.startActivity(intent)
                    }

                    override fun onComplete() {
                        view.setLoadBookDetailsHint(false)
                    }

                    override fun onSubscribe(d: Disposable) {
                        com.add(d)
                        view.setLoadBookDetailsHint(true)
                    }

                })
    }
}
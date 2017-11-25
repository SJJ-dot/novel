package sjj.fiction.books

import android.content.Context
import android.content.Intent
import android.support.v4.content.ContextCompat.startActivity
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import sjj.alog.Log
import sjj.fiction.details.DetailsActivity
import sjj.fiction.model.BookGroup
import sjj.fiction.model.Event
import sjj.fiction.util.bus
import sjj.fiction.util.fictionDataRepository

/**
 * Created by SJJ on 2017/10/22.
 */
class BooksPresenter(private val view: BookrackContract.View) : BookrackContract.Presenter {
    private val com = CompositeDisposable()
    private val data = fictionDataRepository

    init {
        view.setPresenter(this)
    }

    override fun start() {
        data.loadBookGroups().observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<List<BookGroup>> {
                    override fun onError(e: Throwable) {
                        view.setBookListLoadingHint(false)
                        view.setBookListLoadingError(e)
                    }

                    override fun onComplete() {
                        view.setBookListLoadingHint(false)
                    }

                    override fun onNext(t: List<BookGroup>) {
                        view.setBookList(t)
                    }

                    override fun onSubscribe(d: Disposable) {
                        com.add(d)
                        view.setBookListLoadingHint(true)
                    }

                })
        bus.filter { it.id == Event.NEW_BOOK }.subscribe {
            when (it.id) {
                Event.NEW_BOOK -> view.refreshBook(it.value as BookGroup)
            }
        }.also { com.add(it) }
    }

    override fun stop() {
        com.clear()
    }

    override fun onSelectBook(book: BookGroup, context: Context) {
        data.loadBookDetailsAndChapter(book).observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<BookGroup> {
                    override fun onSubscribe(d: Disposable) {
                        com.add(d)
                        view.setBookDetailsLoadingHint(true)
                    }

                    override fun onError(e: Throwable) {
                        view.setBookDetailsLoadingHint(false)
                        view.setBookDetailsLoadingError(e)
                    }

                    override fun onComplete() {
                        view.setBookDetailsLoadingHint(false)
                    }

                    override fun onNext(t: BookGroup) {
                        val intent = Intent(context, DetailsActivity::class.java);
                        intent.putExtra(DetailsActivity.data_book_name, t.bookName)
                        intent.putExtra(DetailsActivity.data_book_author, t.author)
                        context.startActivity(intent)
                    }
                })
    }

    override fun deleteBook(book: BookGroup) {
        data.deleteBookGroup(book.bookName, book.author).observeOn(AndroidSchedulers.mainThread())
                .subscribe({ view.removeBook(it) }, { view.setDeleteBookError(it) }).also { com.add(it) }
    }
}
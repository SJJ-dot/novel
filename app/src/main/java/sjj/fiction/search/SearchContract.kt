package sjj.fiction.search

import io.reactivex.Observable
import sjj.fiction.BasePresenter
import sjj.fiction.BaseView
import sjj.fiction.model.BookGroup

/**
 * Created by SJJ on 2017/10/7.
 */
interface SearchContract {
    interface presenter : BasePresenter {
        fun search(text: String): Observable<List<BookGroup>>
        fun onSelect(book: BookGroup): Observable<BookGroup>
    }

    interface view : BaseView<presenter> {
        fun showBookList(book: List<BookGroup>)
    }
}
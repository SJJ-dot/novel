package sjj.fiction.search

import io.reactivex.Observable
import sjj.fiction.BasePresenter
import sjj.fiction.BaseView
import sjj.fiction.model.Book
import sjj.fiction.model.Url

/**
 * Created by SJJ on 2017/10/7.
 */
interface SearchContract {
    interface presenter : BasePresenter {
        fun search(text: String): Observable<String>
        fun onSelect(book: Book)
        fun onSelect(url: Url)
    }

    interface view : BaseView<presenter> {
        fun showBookList(book: List<Book>)
        fun showBookUrls(book: Book)
    }
}
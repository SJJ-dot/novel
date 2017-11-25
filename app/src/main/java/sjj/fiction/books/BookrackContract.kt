package sjj.fiction.books

import android.content.Context
import sjj.fiction.BasePresenter
import sjj.fiction.BaseView
import sjj.fiction.model.Book
import sjj.fiction.model.BookGroup

/**
 * Created by SJJ on 2017/10/7.
 */
interface BookrackContract {
    interface Presenter : BasePresenter {
        fun onSelectBook(book: BookGroup, context: Context)
        fun deleteBook(book: BookGroup)
    }

    interface View : BaseView<Presenter> {
        fun setBookList(book: List<BookGroup>)
        fun removeBook(book: BookGroup)
        fun refreshBook(book: BookGroup)
        fun setBookListLoadingHint(active: Boolean)
        fun setBookListLoadingError(e: Throwable)
        fun setDeleteBookError(e: Throwable)
        fun setBookDetailsLoadingHint(active: Boolean)
        fun setBookDetailsLoadingError(e: Throwable)
    }
}
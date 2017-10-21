package sjj.fiction.search

import android.content.Context
import io.reactivex.Observable
import sjj.fiction.BasePresenter
import sjj.fiction.BaseView
import sjj.fiction.model.BookGroup

/**
 * Created by SJJ on 2017/10/7.
 */
interface SearchContract {
    interface presenter : BasePresenter {
        fun onSelect(book: BookGroup, context: Context)
    }

    interface view : BaseView<presenter> {
        fun setLoadBookDetailsErrorHint(it: Throwable)
        fun setLoadBookDetailsHint(active: Boolean)
    }
}
package sjj.fiction.main

import android.support.v4.app.FragmentManager
import android.view.View
import sjj.fiction.BasePresenter
import sjj.fiction.BaseView
import sjj.fiction.model.BookGroup

/**
 * Created by SJJ on 2017/10/7.
 */
interface MainContract {
    interface Presenter : BasePresenter {
        fun search(text: String)
    }

    interface View : BaseView<Presenter> {
        fun setSearchBookList(book: List<BookGroup>)
        fun setSearchProgressHint(active:Boolean)
        fun setSearchErrorHint(throwable: Throwable)
    }
}
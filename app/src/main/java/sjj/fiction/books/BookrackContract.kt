package sjj.fiction.books

import sjj.fiction.BasePresenter
import sjj.fiction.BaseView

/**
 * Created by SJJ on 2017/10/7.
 */
interface BookrackContract {
    interface Presenter : BasePresenter {

    }
    interface View : BaseView<Presenter> {

    }
}
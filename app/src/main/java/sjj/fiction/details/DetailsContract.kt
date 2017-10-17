package sjj.fiction.details

import sjj.fiction.BasePresenter
import sjj.fiction.BaseView
import sjj.fiction.model.Book
import sjj.fiction.model.BookGroup
import sjj.fiction.model.Chapter

/**
 * Created by SJJ on 2017/10/17.
 */
interface DetailsContract {
    interface Presenter : BasePresenter {
        fun checkUpdate()
        fun onSelectChapter(index: Int)
        fun onClickChapterListBtn()
        fun onClickOrigin()
        fun onSelectBookOriginItem(index: Int)
    }

    interface View : BaseView<Presenter> {
        fun showBookDetails(book: Book)
        fun showChapters(chapter: List<Chapter>, index: Int, active: Boolean)
        fun isShowChapters(): Boolean
        fun setCheckUpdateIndicator(active: Boolean)
        fun setLoadBookIndicator(active: Boolean)
        fun showErrorMessage(message: String)
        fun showBookContent(bookGroup: BookGroup, index: Int)
        fun showBookOriginItems(items: List<String>)
    }
}
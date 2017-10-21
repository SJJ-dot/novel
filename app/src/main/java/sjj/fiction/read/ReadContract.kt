package sjj.fiction.read

import android.widget.TextView
import sjj.fiction.BasePresenter
import sjj.fiction.BaseView
import sjj.fiction.model.Chapter

/**
 * Created by SJJ on 2017/10/21.
 */
interface ReadContract {
    interface Presenter : BasePresenter {
        fun onContentScrolled(position: Int)
        fun setChapterContent(position: Int)
        fun onSelectChapter(position: Int)
        fun cachedBookChapter()
    }

    interface View : BaseView<Presenter> {
        fun setChapterList(chapter: List<Chapter>)
        fun setChapterListPosition(position: Int)
        fun setChapterContentPosition(position: Int)
        fun setTitle(title: String)
        fun setLoadBookHint(active: Boolean)
        fun setLoadBookHintError(e: Throwable)
        fun setChapterName(name: String)
        fun notifyChapterContentChange()
        fun setCachedBookChapterHint(active: Boolean)
        fun setCachedBookChapterProgressPlus(max: Int)
        fun setCachedBookChapterComplete()
        fun setCachedBookChapterError(e: Throwable)
    }
}
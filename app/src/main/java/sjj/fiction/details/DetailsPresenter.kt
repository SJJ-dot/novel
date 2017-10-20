package sjj.fiction.details

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import sjj.alog.Log
import sjj.fiction.model.BookGroup
import sjj.fiction.util.domain
import sjj.fiction.util.fictionDataRepository

/**
 * Created by SJJ on 2017/10/17.
 */
class DetailsPresenter(private val bookName: String, private val author: String, private val view: DetailsContract.View) : DetailsContract.Presenter {

    private val com = CompositeDisposable()
    private val fictionData = fictionDataRepository
    private var bookGroup: BookGroup? = null
    override fun start() {
        view.setLoadBookIndicator(true)
        fictionData.loadBookGroup(bookName, author)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    bookGroup = it
                    view.showBookDetails(it.currentBook)
                    view.setLoadBookIndicator(false)
                }, {
                    view.showErrorMessage("加载书籍出错：${it.message}")
                    view.setLoadBookIndicator(false)
                    Log.e("details", it)
                })
    }

    override fun stop() {
        com.clear()
    }

    override fun checkUpdate() {
        loadBookDetailsAndChapter(true)
    }

    override fun onSelectChapter(index: Int) {
        view.showBookContent(bookGroup ?: return, index)
    }

    override fun onClickChapterListBtn() {
        val group = bookGroup ?: return
        view.showChapters(group.currentBook.chapterList, group.readIndex, !view.isShowChapters())
    }

    override fun onClickOrigin() {
        val group = bookGroup ?: return
        view.showBookOriginItems(group.books.map { it.url.domain() })
    }

    override fun onSelectBookOriginItem(index: Int) {
        val group = bookGroup ?: return
        val lastBook = group.currentBook
        if (lastBook.id == group.books[index].id) return
        with(group) {
            currentBook = group.books[index]
            bookId = currentBook.id
        }
        fictionData.saveBookGroup(arrayListOf(group))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    loadBookDetailsAndChapter(false)
                }, {
                    view.showErrorMessage("切换源出错：${it.message}")
                    group.currentBook = lastBook
                    group.bookId = lastBook.id
                })

    }

    private fun loadBookDetailsAndChapter(force: Boolean) {
        val group = bookGroup ?: return
        view.setCheckUpdateIndicator(true)
        fictionDataRepository.loadBookDetailsAndChapter(group, force).subscribe({
            view.setCheckUpdateIndicator(false)
            start()
        }, {
            view.setCheckUpdateIndicator(false)
            view.showErrorMessage("检查更新出错：${it.message}")
        })
    }
}
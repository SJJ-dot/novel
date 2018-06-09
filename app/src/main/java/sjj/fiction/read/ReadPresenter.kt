package sjj.fiction.read

import com.raizlabs.android.dbflow.kotlinextensions.save
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import sjj.fiction.data.Repository.fictionDataRepository
import sjj.fiction.model.BookGroup

/**
 * Created by SJJ on 2017/10/21.
 */
class ReadPresenter(private val bookName: String, private val author: String, private val index: Int, private val view: ReadContract.View) : ReadContract.Presenter {
    private val data = fictionDataRepository

    private val com = CompositeDisposable()

    private var book: BookGroup? = null

    init {
        view.setPresenter(this)
    }

    override fun onContentScrolled(position: Int) {
        val book = book ?: return
        view.setChapterName(book.currentBook.chapterList[position].chapterName)
        view.setChapterListPosition(position)
        book.readIndex = position
    }

    override fun onSelectChapter(position: Int) {
        view.setChapterContentPosition(position)
        book?.readIndex = position
    }

    override fun setChapterContent(position: Int) {
        val book = book ?: return
        val chapter = book.currentBook.chapterList[position]
        if (chapter.isLoading) return
        data.loadBookChapter(chapter)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    view.notifyChapterContentChange()
                    chapter.isLoading = false
                }, {
                    chapter.content = "章节内容加载失败：${it.message}"
                    view.notifyChapterContentChange()
                    chapter.isLoading = false
                }).also { com.add(it) }
    }

    override fun cachedBookChapter() {
        val book = book ?: return
        view.setCachedBookChapterHint(true)
        data.cachedBookChapter(book.currentBook)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    view.setCachedBookChapterProgressPlus(book.currentBook.chapterList.size)
                }, {
                    view.setCachedBookChapterHint(false)
                    view.setCachedBookChapterError(it)
                }, {
                    view.setCachedBookChapterHint(false)
                    view.setCachedBookChapterComplete()
                }).also { com.add(it) }
    }

    override fun start() {
        view.setLoadBookHint(true);
        data.loadBookGroup(bookName, author)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    book = it
                    view.setLoadBookHint(false)
                    view.setChapterList(it.currentBook.chapterList)
                    view.setTitle(it.currentBook.name)
                    val index = if (index < it.currentBook.chapterList.size) index else it.currentBook.chapterList.size
                    view.setChapterContentPosition(index)
                    view.setChapterListPosition(index)
                }, {
                    view.setLoadBookHint(false)
                    view.setLoadBookHintError(it)
                }).also { com.add(it) }
    }

    override fun stop() {
        book?.save()
        com.clear()
    }
}

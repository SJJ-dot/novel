package sjj.novel.view.module.read

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import io.reactivex.Flowable
import io.reactivex.Observable
import sjj.novel.data.repository.novelDataRepository
import sjj.novel.model.Book
import sjj.novel.model.BookSourceRecord
import sjj.novel.model.Chapter
import sjj.novel.util.SafeLiveData
import sjj.novel.util.ViewModelDispose
import java.lang.Exception

class DownChapterViewModel(var bookName: String, var bookAuthor: String) : ViewModelDispose() {

    val book = SafeLiveData<Book>()

    val chapterList = SafeLiveData<List<Chapter>>()

    val bookSourceRecord = SafeLiveData<BookSourceRecord>()

    /**
     * 开始下载的起始章节
     */
    val startChapter = SafeLiveData<Chapter?>()

    val endChapter = SafeLiveData<Chapter?>()

    fun initData(): Flowable<Book?> {
        return novelDataRepository.getBookInBookSource(bookName, bookAuthor).flatMap {
            book.setValue(it)
            novelDataRepository.getChapterIntro(it.url).flatMap { list ->
                chapterList.setValue(list)
                endChapter.setValue(list.lastOrNull())
                novelDataRepository.getBookSourceRecord(bookName, bookAuthor).map {
                    bookSourceRecord.setValue(it)
                    startChapter.setValue(list.getOrElse(it.readIndex) { list.lastOrNull() })
                    book.value
                }
            }
        }
    }

    init {
        initData()
                .subscribe()
                .autoDispose("view model Download Chapter init")
    }

    fun cachedBookChapter(): Flowable<Pair<Int, Int>> {
        val start = startChapter.value ?: return Flowable.error(Exception("请设置起始章节"))
        val end = endChapter.value ?: return Flowable.error(Exception("请设置结束章节"))
        return novelDataRepository.cachedBookChapter(start, end)
    }

}
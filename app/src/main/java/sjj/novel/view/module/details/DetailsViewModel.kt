package sjj.novel.view.module.details

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.processors.FlowableProcessor
import sjj.novel.R
import sjj.novel.data.repository.novelDataRepository
import sjj.novel.data.repository.novelSourceRepository
import sjj.novel.model.Book
import sjj.novel.model.Chapter
import sjj.novel.util.host
import sjj.novel.util.resStr

class DetailsViewModel(val name: String, val author: String) : ViewModel() {

    var bookCoverImgUrl = BehaviorProcessor.create<String>()
    val bookName = ObservableField<String>()
    val bookAuthor = ObservableField<String>()
    val bookIntro = ObservableField<String>()
    val lastChapter = ObservableField<String>()
    val origin = ObservableField<String>()
    val isLoading = ObservableBoolean()

    //数据记录
    var book: Book? = null

    fun fillViewModel(): Flowable<Book> = novelDataRepository.getBookInBookSource(name, author).flatMap { book ->
        this.book = book
        bookCoverImgUrl.onNext( book.bookCoverImgUrl)
        bookName.set(book.name)
        bookAuthor.set(book.author)
        bookIntro.set(book.intro)
        isLoading.set(book.loadStatus == Book.LoadState.Loading)
        novelSourceRepository.getBookParse(name, author).map { list ->
            origin.set(R.string.origin_.resStr(list.find { book.url.host.endsWith(it.topLevelDomain) }?.sourceName, list.size))
            book
        }.flatMap {
            novelDataRepository.getLatestChapter(book.url).map { chapter ->
                this.book?.lastChapter = chapter
                lastChapter.set(chapter.chapterName)
                book
            }.switchIfEmpty(Observable.just(book))
        }.toFlowable(BackpressureStrategy.LATEST)
    }


    val bookSourceRecord = novelDataRepository.getBookSourceRecord(name, author)

    fun setReadIndex(index: Chapter): Observable<Int> {
        return novelDataRepository.setReadIndex(name, author, index, 0)
    }

    fun refresh(it: Book): Observable<Book> {
        return novelDataRepository.refreshBook(it.url)
    }
}
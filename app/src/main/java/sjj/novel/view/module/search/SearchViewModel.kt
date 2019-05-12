package sjj.novel.view.module.search

import androidx.lifecycle.ViewModel
import androidx.databinding.ObservableField
import io.reactivex.Observable
import io.reactivex.processors.BehaviorProcessor
import sjj.novel.R
import sjj.novel.data.repository.novelDataRepository
import sjj.novel.data.source.local.localFictionDataSource
import sjj.novel.model.BookSourceRecord
import sjj.novel.model.SearchHistory
import sjj.novel.util.id
import sjj.novel.util.resStr

class SearchViewModel : ViewModel() {

    fun search(text: String) = novelDataRepository.search(text).map { list ->
        list.sortedByDescending { it.books?.size }.map { record ->
            val model = BookSearchItemViewModel()
            model.book = record
            model.bookCover.onNext(record.currentBook?.bookCoverImgUrl)
            model.bookName.set(record.bookName)
            model.author.set(R.string.author_.resStr(record.author))
            model.lastChapter.set(R.string.newest_.resStr(record.currentBook?.lastChapter?.chapterName))
            model.origin.set(R.string.origin_.resStr(record.currentBook?.origin?.sourceName, record.books?.size))
            model
        }
    }

    fun getSearchHistory() = localFictionDataSource.getSearchHistory()

    fun addSearchHistory(searchHistory: SearchHistory) = localFictionDataSource.addSearchHistory(searchHistory)

    fun deleteSearchHistory(searchHistory: List<SearchHistory>): Observable<List<SearchHistory>> = localFictionDataSource.deleteSearchHistory(searchHistory)

    fun saveBookSourceRecord(books: BookSourceRecord) = novelDataRepository.saveBookSourceRecord(books)

    class BookSearchItemViewModel {
        lateinit var book: BookSourceRecord
        val bookCover = BehaviorProcessor.create<String>()
        val bookName = ObservableField<String>()
        val author = ObservableField<String>()
        val lastChapter = ObservableField<String>()
        val origin = ObservableField<String>()

        val id by lazy { book.bookUrl.id }

    }
}
package sjj.novel.search

import android.arch.lifecycle.ViewModel
import android.databinding.ObservableField
import io.reactivex.Observable
import sjj.novel.R
import sjj.novel.data.repository.novelDataRepository
import sjj.novel.data.source.local.localFictionDataSource
import sjj.novel.model.BookSourceRecord
import sjj.novel.model.SearchHistory
import sjj.novel.util.resStr

class SearchViewModel : ViewModel() {

    fun search(text: String) = novelDataRepository.search(text).map {
        it.map { record ->
            val model = BookSearchItemViewModel()
            model.book = record
            model.bookCover.set(record.currentBook?.bookCoverImgUrl)
            model.bookName.set(record.bookName)
            model.author.set(R.string.author_.resStr(record.author))
            model.lastChapter.set(R.string.newest_.resStr(record.currentBook?.lastChapter?.chapterName))
            model.origin.set(R.string.origin_.resStr(record.currentBook?.origin?.sourceName,record.books?.size))
            model
        }
    }

    fun getSearchHistory() = localFictionDataSource.getSearchHistory()

    fun addSearchHistory(searchHistory: SearchHistory) = localFictionDataSource.addSearchHistory(searchHistory)

    fun deleteSearchHistory(searchHistory: List<SearchHistory>): Observable<List<SearchHistory>> = localFictionDataSource.deleteSearchHistory(searchHistory)

    fun saveBookSourceRecord(books: BookSourceRecord) = novelDataRepository.saveBookSourceRecord(books)

    class BookSearchItemViewModel {
        lateinit var book:BookSourceRecord
        val bookCover = ObservableField<String>()
        val bookName = ObservableField<String>()
        val author = ObservableField<String>()
        val lastChapter = ObservableField<String>()
        val origin = ObservableField<String>()

    }
}
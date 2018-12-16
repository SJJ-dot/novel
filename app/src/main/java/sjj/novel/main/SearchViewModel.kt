package sjj.novel.main

import android.arch.lifecycle.ViewModel
import io.reactivex.Observable
import sjj.novel.data.repository.novelDataRepository
import sjj.novel.data.source.local.localFictionDataSource
import sjj.novel.model.Book
import sjj.novel.model.BookSourceRecord
import sjj.novel.model.SearchHistory

class SearchViewModel : ViewModel() {

    fun search(text: String) = novelDataRepository.search(text)

    fun getSearchHistory() = localFictionDataSource.getSearchHistory()

    fun addSearchHistory(searchHistory: SearchHistory) = localFictionDataSource.addSearchHistory(searchHistory)

    fun deleteSearchHistory(searchHistory: List<SearchHistory>): Observable<List<SearchHistory>> = localFictionDataSource.deleteSearchHistory(searchHistory)

    fun saveBookSourceRecord(books: Pair<BookSourceRecord, List<Book>>) = novelDataRepository.saveBookSourceRecord(listOf(books))

}
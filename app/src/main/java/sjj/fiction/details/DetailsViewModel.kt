package sjj.fiction.details

import android.arch.lifecycle.ViewModel
import io.reactivex.Observable
import sjj.fiction.data.repository.fictionDataRepository
import sjj.fiction.model.Book
import sjj.fiction.model.BookSourceRecord

class DetailsViewModel(name: String, author: String) : ViewModel() {
    val book = fictionDataRepository.getBookInBookSource(name, author)
    fun bookSource(book: Book): Observable<List<String>> {
        return fictionDataRepository.getBookSource(book.name, book.author)
    }

    fun setBookSource(book: Book, url: String): Observable<Int> {
        return fictionDataRepository.setBookSource(book.name,book.author,url)
    }

    fun getReadIndex(it: Book): Observable<Int> {
        return fictionDataRepository.getReadIndex(it.name,it.author)
    }

    fun refresh(it: Book) {
        fictionDataRepository.refreshBook(it.url).subscribe()
    }
}
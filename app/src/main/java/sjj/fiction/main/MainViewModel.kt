package sjj.fiction.main

import android.arch.lifecycle.ViewModel
import sjj.fiction.data.repository.fictionDataRepository
import sjj.fiction.model.Book
import sjj.fiction.model.BookSourceRecord

class MainViewModel : ViewModel() {
    val books = fictionDataRepository.getBooks()

    fun delete(book: Book) {
        fictionDataRepository.deleteBook(book.name, book.author)
                .subscribe()
    }

    fun search(text: String) = fictionDataRepository.search(text)

    fun saveBookSourceRecord(books: List<Pair<BookSourceRecord, List<Book>>>)  = fictionDataRepository.saveBookSourceRecord(books)
}
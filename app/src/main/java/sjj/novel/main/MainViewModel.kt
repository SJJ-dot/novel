package sjj.novel.main

import android.arch.lifecycle.ViewModel
import io.reactivex.Observable
import sjj.novel.data.repository.novelDataRepository
import sjj.novel.model.Book
import sjj.novel.model.BookSourceRecord
import sjj.novel.model.Chapter
import sjj.novel.util.concat
import sjj.novel.util.lazyFromIterable
import java.util.concurrent.TimeUnit

class MainViewModel : ViewModel() {
    val books = novelDataRepository.getBooks().flatMap { list ->
        Observable.fromIterable(list).flatMap { b ->
            getLatestChapter(b.url).map {
                b.chapterList = listOf(it)
                b
            }.flatMap {
                novelDataRepository.getReadIndex(it.name, it.author)
                        .firstElement()
                        .map {
                            b.index = it
                            b
                        }
                        .toObservable()
            }
        }.reduce(list) { _, _ -> list }.toFlowable()
    }

    fun delete(book: Book) {
        novelDataRepository.deleteBook(book.name, book.author)
                .subscribe()
    }

    fun search(text: String) = novelDataRepository.search(text)

    fun saveBookSourceRecord(books: List<Pair<BookSourceRecord, List<Book>>>) = novelDataRepository.saveBookSourceRecord(books)

    private fun getLatestChapter(bookUrl: String): Observable<Chapter> {
        return novelDataRepository.getLatestChapter(bookUrl)
    }

    fun getReadIndex(name: String, author: String) = novelDataRepository.getReadIndex(name, author)

    fun refresh(): Observable<Book> {
        return novelDataRepository.getBooks().firstElement().toObservable().flatMap { list ->
            list.forEach {
                it.isLoading = true
            }
            novelDataRepository.batchUpdate(list)
        }.lazyFromIterable {
            novelDataRepository.refreshBook(it.url).delay(500, TimeUnit.MILLISECONDS)
        }.concat()
    }

}
package sjj.novel.main

import android.arch.lifecycle.ViewModel
import io.reactivex.Observable
import io.reactivex.functions.Function
import sjj.novel.data.repository.novelDataRepository
import sjj.novel.model.Book
import sjj.novel.model.BookSourceRecord
import sjj.novel.model.Chapter
import sjj.novel.util.host
import sjj.novel.util.lazyFromIterable
import java.util.concurrent.TimeUnit

class MainViewModel : ViewModel() {
    val books = novelDataRepository.getBooks().flatMap { list ->
        Observable.fromIterable(list).flatMap { b ->
            getLatestChapter(b.url).map {
                b.chapterList = listOf(it)
                b
            }.flatMap { book ->
                novelDataRepository.getReadIndex(book.name, book.author)
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
                it.loadStatus = Book.LoadState.Loading
            }
            novelDataRepository.batchUpdate(list)
                    .flatMap { bookList ->
                        //对小说进行分组使同同一来源的小说一次性发射出去
                        val map = mutableMapOf<String, MutableList<Book>>()
                        bookList.forEach {
                            map.getOrPut(it.url.host) { mutableListOf() }.add(it)
                        }
                        Observable.fromIterable(map.values)
                                .lazyFromIterable { book ->
                                    novelDataRepository.refreshBook(book.url)
                                            .delay(500, TimeUnit.MILLISECONDS)
                                            .onErrorResumeNext(Function { _ ->
                                                book.loadStatus = Book.LoadState.LoadFailed
                                                novelDataRepository.batchUpdate(listOf(book)).map { it.first() }
                                            })
                                }.flatMap { it }
                    }
        }
    }

}
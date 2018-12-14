package sjj.novel.main

import android.arch.lifecycle.ViewModel
import io.reactivex.Observable
import io.reactivex.functions.Function
import sjj.novel.data.repository.novelDataRepository
import sjj.novel.data.source.local.localFictionDataSource
import sjj.novel.model.Book
import sjj.novel.model.BookSourceRecord
import sjj.novel.model.Chapter
import sjj.novel.model.SearchHistory
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
                novelDataRepository.getBookSourceRecord(book.name, book.author)
                        .firstElement()
                        .map {
                            b.index = it.readIndex
                            b.isThrough = it.isThrough
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

    fun getSearchHistory() = localFictionDataSource.getSearchHistory()

    fun addSearchHistory(searchHistory: SearchHistory) = localFictionDataSource.addSearchHistory(searchHistory)

    fun deleteSearchHistory(searchHistory: List<SearchHistory>): Observable<List<SearchHistory>> = localFictionDataSource.deleteSearchHistory(searchHistory)

    fun saveBookSourceRecord(books: Pair<BookSourceRecord, List<Book>>) = novelDataRepository.saveBookSourceRecord(listOf(books))

    private fun getLatestChapter(bookUrl: String): Observable<Chapter> {
        return novelDataRepository.getLatestChapter(bookUrl)
    }

    fun refresh(): Observable<Book> {
        return novelDataRepository.getBooks()
                .firstElement()
                .toObservable()
                .flatMap { list ->
                    list.forEach {
                        it.loadStatus = Book.LoadState.Loading
                    }
                    val disposed = list.toMutableList()
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
                                                    }).doOnNext {
                                                        disposed.remove(it)
                                                    }
                                        }.flatMap { it }
                            }.doOnDispose {
                                list.forEach {
                                    it.loadStatus = Book.LoadState.UnLoad
                                }
                                //刷新被取消的时候将正在加载的数据改为未加载
                                novelDataRepository.batchUpdate(list).subscribe()
                            }
                }
    }

}
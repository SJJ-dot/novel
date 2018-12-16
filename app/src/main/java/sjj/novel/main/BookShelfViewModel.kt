package sjj.novel.main

import android.arch.lifecycle.ViewModel
import android.databinding.ObservableField
import io.reactivex.Observable
import io.reactivex.functions.Function
import sjj.novel.R
import sjj.novel.data.repository.novelDataRepository
import sjj.novel.model.Book
import sjj.novel.util.host
import sjj.novel.util.lazyFromIterable
import sjj.novel.util.resStr
import java.util.concurrent.TimeUnit

class BookShelfViewModel : ViewModel() {
    val books = novelDataRepository.getBooks().flatMap { list ->
        Observable.fromIterable(list).flatMap { b ->
            novelDataRepository.getLatestChapter(b.url).map {
                b.chapterList = listOf(it)
                b
            }.flatMap { book ->
                novelDataRepository.getBookSourceRecord(book.name, book.author)
                        .firstElement()
                        .map {
                            b.index = it.readIndex
                            b.readChapterName = it.chapterName
                            b.isThrough = it.isThrough
                            b
                        }
                        .toObservable()
            }
        }.reduce(list) { _, _ -> list }.map {
            it.map { book ->
                val model = BookShelfItemViewModel()
                model.book = book
                model.bookName.set(book.name)
                model.author.set(R.string.author_.resStr(book.author))
                model.lastChapter.set(R.string.newest_.resStr(book.chapterList.lastOrNull()?.chapterName))
                model.haveRead.set(R.string.haveRead_.resStr(book.readChapterName))
                model.bookCover.set(book.bookCoverImgUrl)
                model
            }
        }.toFlowable()
    }

    /**
     *  刷新书籍书籍。
     */
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


    fun delete(book: Book) {
        novelDataRepository.deleteBook(book.name, book.author)
                .subscribe()
    }

    class BookShelfItemViewModel {
        lateinit var book:Book
        val bookCover = ObservableField<String>()
        val bookName = ObservableField<String>()
        val author = ObservableField<String>()
        val lastChapter = ObservableField<String>()
        val haveRead = ObservableField<String>()

    }

}
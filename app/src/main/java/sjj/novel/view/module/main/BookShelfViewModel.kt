package sjj.novel.view.module.main

import androidx.lifecycle.ViewModel
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import io.reactivex.Flowable
import io.reactivex.Observable
import sjj.novel.R
import sjj.novel.data.repository.novelDataRepository
import sjj.novel.data.repository.novelSourceRepository
import sjj.novel.data.source.local.localFictionDataSource
import sjj.novel.model.Book
import sjj.novel.model.BookSourceRecord
import sjj.novel.model.Chapter
import sjj.novel.util.*
import java.util.concurrent.TimeUnit

class BookShelfViewModel : ViewModel() {
    val books: Flowable<List<BookShelfItemViewModel>> = novelDataRepository.getBooks().flatMap { list ->

        val map = list.map { book ->
            BookShelfItemViewModel().apply {
                this.book = book
                bookName.set(book.name)
                author.set(R.string.author_.resStr(book.author))
                bookCover.set(book.bookCoverImgUrl)
                loading.set(book.loadStatus == Book.LoadState.Loading)
            }
        }

        Observable.fromIterable(map).flatMap {
            novelDataRepository.getLatestChapter(it.book.url).map { chapter ->
                it.lastChapter.set(R.string.newest_.resStr(chapter.chapterName))
                it.book.lastChapter = chapter
                it
            }.switchIfEmpty(Observable.just(it))
        }.flatMap { model ->
            novelDataRepository.getBookSourceRecord(model.book.name, model.book.author).firstElement().toObservable().map {
                model.bookSourceRecord = it
                model.haveRead.set(R.string.haveRead_.resStr(it.chapterName))

                model.remainingChapter.set(maxOf((model.book.lastChapter?.index
                        ?: 0) - it.readIndex + (if (it.isThrough) 0 else 1), 0))
                model
            }
        }.flatMap { model ->
            novelSourceRepository.getBookParse(model.book.name, model.book.author).map { list ->
                model.origin.set(R.string.origin_.resStr(list.find { model.book.url.host.endsWith(it.topLevelDomain) }?.sourceName, list.size))
                model
            }
        }.toList().toFlowable().map { mutableList ->
            mutableList.map { it.bookSourceRecord.sequence }.log()
            mutableList.sortBy { it.bookSourceRecord.sequence }
            mutableList
        }
    }

    /**
     *  刷新书籍书籍。
     */
    fun refresh(): Observable<Book> {
        return novelDataRepository.getBooks()
                .firstElement()
                .toObservable()
                .flatMap { list ->
                    //对小说进行分组使同同一来源的小说一次性发射出去
                    val map = mutableMapOf<String, MutableList<Book>>()
                    list.forEach {
                        map.getOrPut(it.url.host) { mutableListOf() }.add(it)
                    }
                    Observable.fromIterable(map.values)
                            .lazyFromIterable { book ->
                                novelDataRepository.refreshBook(book.url)
                                        .delay(500, TimeUnit.MILLISECONDS)
                            }.flatMap { it }
                }
    }

    fun setReadIndex(index: Chapter, book: Book): Observable<Int> {
        return novelDataRepository.setReadIndex(book.name, book.author, index, 0)
    }

    fun updateBookSourceRecordSeq(list: List<BookSourceRecord>): Observable<List<BookSourceRecord>> {
        return localFictionDataSource.updateBookSourceRecordSeq(list)
    }


    fun delete(book: Book) {
        novelDataRepository.deleteBook(book.name, book.author)
                .subscribe()
    }

    class BookShelfItemViewModel {
        lateinit var book: Book
        lateinit var bookSourceRecord: BookSourceRecord

        val bookCover = ObservableField<String>()
        val bookName = ObservableField<String>()
        val author = ObservableField<String>()
        val lastChapter = ObservableField<String>()
        val haveRead = ObservableField<String>()
        val remainingChapter = ObservableInt()
        val origin = ObservableField<String>()
        val loading = ObservableBoolean()
        val id: Long by lazy { book.url.id }
    }

}
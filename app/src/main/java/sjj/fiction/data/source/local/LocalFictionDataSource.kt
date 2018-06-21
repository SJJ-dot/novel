package sjj.fiction.data.source.local

import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import sjj.fiction.data.repository.FictionDataRepository
import sjj.fiction.model.Book
import sjj.fiction.model.BookSourceRecord
import sjj.fiction.model.Chapter

/**
 * Created by SJJ on 2017/10/15.
 */
class LocalFictionDataSource : FictionDataRepository.LocalSource {


    private val bookDao by lazy { booksDataBase.bookDao() }

    override fun saveBookSourceRecord(books: List<Pair<BookSourceRecord, List<Book>>>): Single<List<Book>> {
        return Single.fromCallable {
            booksDataBase.runInTransaction {
                bookDao.insertRecordAndBooks(books.map {
                    it.first
                }, books.map {
                    it.second.toMutableList()
                }.reduce { acc, list ->
                    acc.addAll(list)
                    acc
                })
            }
        }.flatMap {
            bookDao.getBooksInRecord().firstOrError()
        }
    }

    override fun getBook(url: String): Flowable<Book> {
        return bookDao.getBook(url).doOnNext {
            it.chapterList = bookDao.getChapters(url)
        }
    }

    override fun insertBook(book: Book): Observable<Book> {
        return Observable.fromCallable {
            booksDataBase.runInTransaction {
                bookDao.insertBook(book)
                bookDao.insertChapters(book.chapterList)
            }
            book
        }.subscribeOn(Schedulers.io())
    }

    override fun getChapter(url: String): Flowable<Chapter> {
        return bookDao.getChapter(url)
    }

    override fun updateChapter(chapter: Chapter): Observable<Chapter> {
        return Observable.fromCallable {
            bookDao.updateChapter(chapter)
            chapter
        }
    }

    override fun getAllReadingBook(): Flowable<List<Book>> {
        return bookDao.getBooksInRecord()
    }

    override fun deleteBook(bookName: String, author: String): Observable<Int> {
        return Observable.fromCallable {
            bookDao.deleteBook(bookName, author)

        }
    }
}
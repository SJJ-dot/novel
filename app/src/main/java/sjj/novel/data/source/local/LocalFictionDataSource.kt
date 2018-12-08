package sjj.novel.data.source.local

import android.arch.paging.DataSource
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import sjj.novel.data.repository.NovelDataRepository
import sjj.novel.model.Book
import sjj.novel.model.BookSourceRecord
import sjj.novel.model.Chapter
import sjj.novel.util.fromCallableOrNull

/**
 * Created by SJJ on 2017/10/15.
 */
class LocalFictionDataSource : NovelDataRepository.LocalSource {


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
        }.subscribeOn(Schedulers.io()).flatMap {
            bookDao.getBooksInRecord().firstOrError()
        }
    }

    override fun getBookSource(name: String, author: String): Observable<List<String>> {
        return fromCallableOrNull {
            bookDao.getBookSource(name, author)
        }.subscribeOn(Schedulers.io())
    }

    override fun updateBookSource(name: String, author: String, url: String): Observable<Int> {
        return fromCallableOrNull {
            bookDao.updateBookSource(name, author, url)
        }.subscribeOn(Schedulers.io())
    }

    override fun getBookInBookSource(name: String, author: String): Flowable<Book> {
        return bookDao.getBookInBookSource(name, author).flatMap {
            getChapterIntro(it.url).first(listOf()).map { c ->
                it.chapterList = c
                it
            }.toFlowable()
        }
    }

    override fun getReadIndex(name: String, author: String): Flowable<Int> {
        return bookDao.getReadIndex(name, author)
    }

    override fun setReadIndex(name: String, author: String, index: Int): Observable<Int> {
        return fromCallableOrNull {
            bookDao.setReadIndex(name, author, index)
        }.subscribeOn(Schedulers.io())
    }

    override fun refreshBook(book: Book): Observable<Book> {
        return fromCallableOrNull {
            booksDataBase.runInTransaction {
                bookDao.updateBook(book)
                bookDao.insertChapters(book.chapterList)
            }
            book
        }.subscribeOn(Schedulers.io())
    }

    override fun getLatestChapter(bookUrl: String): Observable<Chapter> {
        return fromCallableOrNull {
            bookDao.getLatestChapter(bookUrl)
        }.subscribeOn(Schedulers.io())
    }

    override fun getChapter(url: String): Observable<Chapter> {
        return fromCallableOrNull {
            bookDao.getChapter(url)
        }.subscribeOn(Schedulers.io())
    }

    override fun getChapters(bookUrl: String): DataSource.Factory<Int, Chapter> {
        return bookDao.getChapters(bookUrl)
    }

    override fun getChapterIntro(bookUrl: String): Flowable<List<Chapter>> {
        return bookDao.getChapterIntro(bookUrl)
    }

    override fun getUnLoadChapters(bookUrl: String): Observable<List<Chapter>> {
        return fromCallableOrNull {
            bookDao.getUnLoadChapters(bookUrl)
        }.subscribeOn(Schedulers.io())
    }

    override fun updateChapter(chapter: Chapter): Observable<Chapter> {
        return fromCallableOrNull {
            bookDao.updateChapter(chapter)
            chapter
        }.subscribeOn(Schedulers.io())
    }

    override fun getAllReadingBook(): Flowable<List<Book>> {
        return bookDao.getBooksInRecord()
    }

    override fun deleteBook(bookName: String, author: String): Observable<Int> {
        return fromCallableOrNull {
            bookDao.deleteBook(bookName, author)
        }.subscribeOn(Schedulers.io())
    }
}
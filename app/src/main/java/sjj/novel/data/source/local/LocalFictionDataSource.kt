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
import sjj.novel.model.SearchHistory
import sjj.novel.util.fromCallableOrNull

/**
 * Created by SJJ on 2017/10/15.
 */
val localFictionDataSource by lazy { LocalFictionDataSource() }

class LocalFictionDataSource : NovelDataRepository.LocalSource {


    private val bookDao by lazy { booksDataBase.bookDao() }

    override fun saveBookSourceRecord(books: BookSourceRecord): Single<List<Book>> {
        return Single.fromCallable {
            booksDataBase.runInTransaction {
                bookDao.insertRecordAndBooks(books, books.books!!)
            }
        }.subscribeOn(Schedulers.io()).flatMap {
            bookDao.getBooksInRecord().firstOrError()
        }
    }

    override fun getBookSource(name: String, author: String): Observable<List<String>> {
        return bookDao.getBookSource(name, author)
                .firstElement()
                .toObservable()
                .map { it.map { it.url } }
                .subscribeOn(Schedulers.io())
    }

    /**
     * 根据书名与作者获取不同来源的所有书籍
     */
    fun getBooks(name: String, author: String): Flowable<List<Book>> {
        return bookDao.getBookSource(name, author).subscribeOn(Schedulers.io())
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

    override fun getBookSourceRecord(name: String, author: String): Flowable<BookSourceRecord> {
        return bookDao.getBookSourceRecord(name, author)
    }

    override fun setReadIndex(name: String, author: String, index: Chapter, isThrough: Boolean): Observable<Int> {
        return fromCallableOrNull {
            bookDao.setReadIndex(name, author, index.index, index.chapterName, isThrough)
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

    override fun batchUpdate(book: List<Book>): Observable<List<Book>> {
        return fromCallableOrNull {
            booksDataBase.runInTransaction {
                book.forEach {
                    bookDao.updateBook(it)
                }
            }
            book
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

    /**
     * 获取搜索历史列表
     */
    fun getSearchHistory(): Flowable<List<SearchHistory>> {
        return booksDataBase.searchHistoryDao().getSearchHistory()
    }

    /**
     * 添加一个搜索纪录
     */
    fun addSearchHistory(searchHistory: SearchHistory): Observable<SearchHistory> {
        return fromCallableOrNull {
            booksDataBase.searchHistoryDao().addSearchHistory(searchHistory)
            searchHistory
        }.subscribeOn(Schedulers.io())
    }

    /**
     * 删除一个搜索纪录
     */
    fun deleteSearchHistory(searchHistory: List<SearchHistory>): Observable<List<SearchHistory>> {
        return fromCallableOrNull {
            booksDataBase.searchHistoryDao().deleteSearchHistory(searchHistory)
            searchHistory
        }.subscribeOn(Schedulers.io())
    }

}
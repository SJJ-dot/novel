package sjj.fiction.data.Repository

import io.reactivex.Observable
import sjj.fiction.data.source.DataSourceInterface
import sjj.fiction.model.Book
import sjj.fiction.model.BookGroup
import sjj.fiction.model.Chapter

/**
 * Created by SJJ on 2017/9/2.
 */
interface FictionDataRepository : DataRepositoryInterface {
    fun search(search: String): Observable<List<BookGroup>>
    fun setSearchHistory(value: List<String>): Observable<List<String>>
    fun getSearchHistory(): Observable<List<String>>
    fun loadBookDetailsAndChapter(book: BookGroup, force: Boolean = false): Observable<BookGroup>
    fun loadBookChapter(chapter: Chapter): Observable<Chapter>
    fun loadBookGroups(): Observable<List<BookGroup>>
    fun loadBookGroup(bookName: String, author: String): Observable<BookGroup>
    fun saveBookGroup(book: List<BookGroup>): Observable<List<BookGroup>>
    interface RemoteSource : Base {
        fun domain(): String
        fun search(search: String): Observable<List<Book>>
    }

    interface SourceLocal : Base {
        fun setSearchHistory(value: List<String>): Observable<List<String>>
        fun getSearchHistory(): Observable<List<String>>
        fun saveBookGroup(book: List<BookGroup>): Observable<List<BookGroup>>
        fun updateBookGroup(book: BookGroup): Observable<BookGroup>
        fun updateBook(book: Book): Observable<Book>
        fun saveChapter(chapter: Chapter): Observable<Chapter>
        fun loadBookGroups(): Observable<List<BookGroup>>
        fun loadBookGroup(bookName: String, author: String): Observable<BookGroup>
    }

    interface Base : DataSourceInterface {
        fun loadBookChapter(chapter: Chapter): Observable<Chapter>
        fun loadBookDetailsAndChapter(book: Book): Observable<Book>
    }
}
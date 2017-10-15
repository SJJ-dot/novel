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
    fun getSearchHistory(): Observable<List<String>>
    fun loadBookDetailsAndChapter(book: BookGroup): Observable<BookGroup>
    fun loadBookChapter(chapter: Chapter): Observable<Chapter>
    interface RemoteSource : Base {
        fun domain(): String
    }

    interface SourceLocal : Base {
        fun setSearchHistory(value: List<String>): Observable<List<String>>
        fun getSearchHistory(): Observable<List<String>>
    }

    interface Base : DataSourceInterface {
        fun search(search: String): Observable<List<Book>>
        fun loadBookDetailsAndChapter(book: Book): Observable<Book>
        fun loadBookChapter(chapter: Chapter): Observable<Chapter>
    }
}
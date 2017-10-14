package sjj.fiction.data.Repository

import io.reactivex.Observable
import sjj.fiction.data.source.DataSourceInterface
import sjj.fiction.model.Book
import sjj.fiction.model.BookGroup
import sjj.fiction.model.Chapter
import sjj.fiction.model.Url

/**
 * Created by SJJ on 2017/9/2.
 */
interface FictionDataRepository : DataRepositoryInterface {
    fun search(search: String): Observable<List<BookGroup>>
    fun loadBookDetailsAndChapter(book: BookGroup): Observable<BookGroup>
    fun loadBookChapter(chapter: Chapter): Observable<Chapter>
    interface Source : DataSourceInterface {
        fun domain(): Url
        fun search(search: String): Observable<List<Book>>
        fun loadBookDetailsAndChapter(book: Book): Observable<Book>
        fun loadBookChapter(chapter: Chapter): Observable<Chapter>
    }
}
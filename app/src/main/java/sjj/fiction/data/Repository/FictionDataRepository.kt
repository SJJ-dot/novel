package sjj.fiction.data.Repository

import io.reactivex.Observable
import sjj.fiction.data.source.DataSourceInterface
import sjj.fiction.model.Book
import sjj.fiction.model.Chapter
import sjj.fiction.model.SearchResultBook

/**
 * Created by SJJ on 2017/9/2.
 */
interface FictionDataRepository : DataRepositoryInterface {
    fun search(search: String): Observable<List<SearchResultBook>>
    fun loadBookDetailsAndChapter(searchResultBook: SearchResultBook): Observable<Book>
    fun loadBookChapter(chapter: Chapter): Observable<Chapter>
    interface Source : DataSourceInterface {
        fun search(search: String): Observable<List<SearchResultBook>>
        fun loadBookDetailsAndChapter(searchResultBook: SearchResultBook): Observable<Book>
        fun loadBookChapter(chapter: Chapter): Observable<Chapter>
    }
}
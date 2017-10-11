package sjj.fiction.data.Repository.impl

import io.reactivex.Observable
import sjj.fiction.data.Repository.FictionDataRepository
import sjj.fiction.data.source.remote.dhzw.DhzwDataSource
import sjj.fiction.model.Book
import sjj.fiction.model.Chapter
import sjj.fiction.model.SearchResultBook

/**
 * Created by SJJ on 2017/9/3.
 */
class FictionDataRepositoryImpl : FictionDataRepository {
    private val source: FictionDataRepository.Source = DhzwDataSource()

    override fun search(search: String): Observable<List<SearchResultBook>> = source.search(search)
    override fun loadBookDetailsAndChapter(searchResultBook: SearchResultBook): Observable<Book> = source.loadBookDetailsAndChapter(searchResultBook)
    override fun loadBookChapter(chapter: Chapter): Observable<Chapter> = source.loadBookChapter(chapter)
}
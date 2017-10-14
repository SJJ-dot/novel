package sjj.fiction.search

import io.reactivex.Observable
import sjj.fiction.data.Repository.FictionDataRepository
import sjj.fiction.data.source.remote.dhzw.DhzwDataSource
import sjj.fiction.data.source.remote.yunlaige.YunlaigeDataSource
import sjj.fiction.model.BookGroup
import sjj.fiction.util.errorObservable
import sjj.fiction.util.fictionDataRepository

/**
 * Created by SJJ on 2017/10/8.
 */
class SearchPresenter(private val view: SearchContract.view) : SearchContract.presenter {
    private val sources = arrayOf<FictionDataRepository.Source>(DhzwDataSource(), YunlaigeDataSource())
    private var data: FictionDataRepository? = null
    override fun start() {
        data = fictionDataRepository
    }

    override fun stop() {
        data = null
    }

    override fun search(text: String): Observable<List<BookGroup>> = data?.search(text) ?: errorObservable("this presenter not start")

    override fun onSelect(book: BookGroup): Observable<BookGroup> = data?.loadBookDetailsAndChapter(book) ?: errorObservable("this presenter not start")
}
package sjj.fiction.search

import io.reactivex.Observable
import sjj.fiction.data.Repository.SoduDataRepository
import sjj.fiction.model.Book
import sjj.fiction.model.SearchResultBook
import sjj.fiction.model.Url
import sjj.fiction.util.DATA_REPOSITORY_SODU
import sjj.fiction.util.DataRepository
import sjj.fiction.util.def

/**
 * Created by SJJ on 2017/10/8.
 */
class SearchPresenter(private val view: SearchContract.view) : SearchContract.presenter {
    private var data: SoduDataRepository? = null
    override fun start() {
        data = DataRepository[DATA_REPOSITORY_SODU]
    }

    override fun stop() {
        data = null
    }

    override fun search(text: String): Observable<List<SearchResultBook>> = data?.search(text) ?: def<List<SearchResultBook>> { throw Exception("this presenter not start") }

    override fun onSelect(book: SearchResultBook) {

    }

    override fun onSelect(url: Url) {

    }

}
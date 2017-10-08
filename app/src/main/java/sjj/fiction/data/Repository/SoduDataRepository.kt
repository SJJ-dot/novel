package sjj.fiction.data.Repository

import io.reactivex.Observable
import sjj.fiction.data.DataRepositoryInterface
import sjj.fiction.data.DataSourceInterface
import sjj.fiction.model.SearchResultBook

/**
 * Created by SJJ on 2017/9/2.
 */
interface SoduDataRepository : DataRepositoryInterface {
    fun search(search: String): Observable<List<SearchResultBook>>
    interface Source : DataSourceInterface {
        fun search(search: String): Observable<List<SearchResultBook>>
    }
}
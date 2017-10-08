package sjj.fiction.data.Repository.impl

import io.reactivex.Observable
import sjj.fiction.data.Repository.SoduDataRepository
import sjj.fiction.data.source.remote.SoduDataSource
import sjj.fiction.model.SearchResultBook

/**
 * Created by SJJ on 2017/9/3.
 */
class SoduDataRepositoryImpl :SoduDataRepository{
    private val source:SoduDataRepository.Source = SoduDataSource()
    override fun search(search: String): Observable<List<SearchResultBook>> = source.search(search)
}
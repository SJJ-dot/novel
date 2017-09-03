package sjj.fiction.data.Repository

import sjj.fiction.data.source.DataSourceInterface

/**
 * Created by SJJ on 2017/9/2.
 */
interface SoduDataRepository :DataRepositoryInterface{
    fun search(search: String): String;
    interface Source : DataSourceInterface {
        fun search(search: String): String;
    }
}
package sjj.fiction.data.Repository

import io.reactivex.Observable

interface TestDataRepository : DataRepositoryInterface {
    fun hello(url: String): Observable<String>
    interface RemoteSource {
        fun hello(url: String): Observable<String>
    }
}
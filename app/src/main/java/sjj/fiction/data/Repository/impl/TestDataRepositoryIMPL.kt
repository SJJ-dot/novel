package sjj.fiction.data.Repository.impl

import io.reactivex.Observable
import sjj.fiction.data.Repository.TestDataRepository
import sjj.fiction.data.source.remote.test.TestDataSource

class TestDataRepositoryIMPL : TestDataRepository {
    private val source: TestDataRepository.RemoteSource = TestDataSource()
    override fun hello(url: String): Observable<String> {
        return source.hello(url)
    }
}
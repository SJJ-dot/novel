package sjj.fiction.data.source.remote.test

import io.reactivex.Observable
import sjj.fiction.data.Repository.TestDataRepository
import sjj.fiction.data.source.remote.HttpDataSource
import sjj.fiction.data.source.remote.TestHttpInterface

class TestDataSource : HttpDataSource(), TestDataRepository.RemoteSource {
    private val server = create<TestHttpInterface>()
    override fun baseUrl(): String {
        return "http://192.168.1.2:8080"
    }

    override fun hello(url: String): Observable<String> {
        return server.hello(url)
    }
}
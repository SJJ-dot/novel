package sjj.fiction.util

import android.content.Context
import sjj.fiction.data.Repository.DataRepositoryInterface
import sjj.fiction.data.Repository.impl.FictionDataRepositoryImpl
import sjj.fiction.model.Session


/**
 * Created by sjj on 2017/8/2.
 */
public val DATA_REPOSITORY_FICTION = "DATA_REPOSITORY_FICTION"
object DataRepository {
    private val session = Session<DataRepositoryInterface>()

    fun initDataSource(context: Context) {
        session[DATA_REPOSITORY_FICTION] = FictionDataRepositoryImpl()
    }

    operator fun set(key: String, repository: DataRepositoryInterface) {
        session[key] = repository
    }

    fun <T : DataRepositoryInterface> remove(key: String): T {
        return session.remove<T>(key)
    }

    operator fun <T : DataRepositoryInterface> get(key: String): T {
        return session.get<T>(key)
    }

}

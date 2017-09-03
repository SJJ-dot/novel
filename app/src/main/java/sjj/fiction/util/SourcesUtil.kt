package sjj.fiction.util

import android.content.Context

import sjj.fiction.data.Repository.DataRepositoryInterface
import sjj.fiction.data.Repository.SoduDataRepository
import sjj.fiction.data.Repository.impl.SoduDataRepositoryImpl
import sjj.fiction.model.Session


/**
 * Created by sjj on 2017/8/2.
 */
public val keySoduDataRepository = "SoduDataRepository"
object SourcesUtil {
    private val session = Session<DataRepositoryInterface>()

    fun initDataSource(context: Context) {
        session[keySoduDataRepository] = SoduDataRepositoryImpl()
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

    fun destroy() {
        val map = session.clear()
        for (repository in map.values) {
            repository.destroy()
        }
    }

}

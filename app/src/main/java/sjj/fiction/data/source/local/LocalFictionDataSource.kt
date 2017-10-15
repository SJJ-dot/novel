package sjj.fiction.data.source.local

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Observable
import sjj.alog.Log
import sjj.fiction.App
import sjj.fiction.data.Repository.FictionDataRepository
import sjj.fiction.model.Book
import sjj.fiction.model.Chapter
import sjj.fiction.util.def

/**
 * Created by SJJ on 2017/10/15.
 */
class LocalFictionDataSource : FictionDataRepository.SourceLocal {
    private val KEY_SEARCH_HISTORY = "LOCAL_FICTION_DATA_SOURCE_KEY_SEARCH_HISTORY"

    private val config = App.app.config

    private val gson = Gson()

    init {
    }

    override fun setSearchHistory(value: List<String>): Observable<List<String>> = def {
        set(KEY_SEARCH_HISTORY, value)
        value
    }


    override fun getSearchHistory(): Observable<List<String>> = def {
        get<List<String>>(KEY_SEARCH_HISTORY) ?: listOf()
    }

    override fun search(search: String): Observable<List<Book>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun loadBookDetailsAndChapter(book: Book): Observable<Book> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun loadBookChapter(chapter: Chapter): Observable<Chapter> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun set(key: String, value: Any) {
        config.userEditor.putString(key, gson.toJson(value)).commit()
    }

    private fun <T> get(key: String): T? = gson.fromJson<T>(config.userSp.getString(key, ""), (object : TypeToken<T>() {}).type)

}
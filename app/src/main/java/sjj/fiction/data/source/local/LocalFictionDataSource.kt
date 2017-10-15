package sjj.fiction.data.source.local

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.raizlabs.android.dbflow.kotlinextensions.*
import io.reactivex.Observable
import sjj.alog.Log
import sjj.fiction.App
import sjj.fiction.BookDataBase
import sjj.fiction.data.Repository.FictionDataRepository
import sjj.fiction.model.Book
import sjj.fiction.model.BookGroup
import sjj.fiction.model.Book_Table
import sjj.fiction.model.Chapter
import sjj.fiction.util.def

/**
 * Created by SJJ on 2017/10/15.
 */
class LocalFictionDataSource : FictionDataRepository.SourceLocal {
    private val KEY_SEARCH_HISTORY = "LOCAL_FICTION_DATA_SOURCE_KEY_SEARCH_HISTORY"

    private val config = App.app.config

    private val gson = Gson()

    override fun setSearchHistory(value: List<String>): Observable<List<String>> = def {
        set(KEY_SEARCH_HISTORY, value)
        value
    }

    override fun getSearchHistory(): Observable<List<String>> = def {
        get<List<String>>(KEY_SEARCH_HISTORY) ?: listOf()
    }


    override fun saveBookGroup(book: List<BookGroup>): Observable<List<BookGroup>> {
        return def {
            database<BookDataBase>().executeTransaction {
                book.forEach { bookG ->
                    bookG.save(it)
                    bookG.books.forEach { b ->
                        b.save(it)
                    }
                }
            }
            book
        }
    }

    override fun updateBookGroup(book: BookGroup): Observable<BookGroup> {
        return def {
            book.update()
            book
        }
    }

    override fun updateBook(book: Book): Observable<Book> {
        return def {
            database<BookDataBase>().executeTransaction {
                book.update(it)
                book.chapterList.forEach { chapter: Chapter ->
                    chapter.save(it)
                }
            }
            book
        }
    }

    override fun saveChapter(chapter: Chapter): Observable<Chapter> {
        return def {
            chapter.update()
            chapter
        }
    }

    override fun loadBookChapter(chapter: Chapter): Observable<Chapter> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun loadBookGroups(): Observable<List<BookGroup>> {
        return def {
            val listGroup = mutableListOf<BookGroup>()
            database<BookDataBase>().executeTransaction {
                val list = (select from BookGroup::class).list
                list.forEach {
                    it.currentBook = (select from Book::class where (Book_Table.id eq it.bookId)).result ?: it.currentBook
                    it.books = (select from Book::class where (Book_Table.name eq it.bookName) and (Book_Table.author eq it.author)).list
                }
                listGroup.addAll(list)
            }
            return@def listGroup
        }
    }

    private fun set(key: String, value: Any) {
        config.userEditor.putString(key, gson.toJson(value)).commit()
    }

    private fun <T> get(key: String): T? = gson.fromJson<T>(config.userSp.getString(key, ""), (object : TypeToken<T>() {}).type)

}
package sjj.fiction.data.source.local

import android.arch.persistence.room.*
import io.reactivex.Flowable
import sjj.fiction.App
import sjj.fiction.model.Book
import sjj.fiction.model.BookSourceRecord
import sjj.fiction.model.Chapter


@Dao
interface BookDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertRecordAndBooks(bookSource: List<BookSourceRecord>, books: List<Book>)

    @Query("select * from Book where url in (select bookUrl from BookSourceRecord)")
    fun getBooksInRecord(): Flowable<List<Book>>


    @Query("SELECT * FROM Book WHERE url=:url")
    fun getBook(url: String): Flowable<Book>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBook(book: Book)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertChapters(book: List<Chapter>)

    @Query("select url,bookUrl,`index`,chapterName,isLoadSuccess from Chapter where bookUrl=:bookUrl")
    fun getChapters(bookUrl: String): List<Chapter>

    @Query("select * from Chapter where url=:url")
    fun getChapter(url: String): Flowable<Chapter>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateChapter(chapter: Chapter)

    @Query("delete from BookSourceRecord where bookName=:bookName and author=:author")
    fun deleteBook(bookName: String, author: String):Int

}


@Database(entities = [Book::class, BookSourceRecord::class, Chapter::class], version = 1)
abstract class BooksDataBase : RoomDatabase() {
    abstract fun bookDao(): BookDao
}

val booksDataBase by lazy {
    Room.databaseBuilder(App.app, BooksDataBase::class.java, "books.db").fallbackToDestructiveMigration()
            .build()
}
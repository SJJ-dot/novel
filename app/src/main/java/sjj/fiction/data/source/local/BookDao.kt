package sjj.fiction.data.source.local

import android.arch.paging.DataSource
import android.arch.persistence.room.*
import io.reactivex.Flowable
import sjj.fiction.App
import sjj.fiction.model.Book
import sjj.fiction.model.BookSourceRecord
import sjj.fiction.model.Chapter


@Dao
interface BookDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRecordAndBooks(bookSource: List<BookSourceRecord>, books: List<Book>)

    @Query("select * from Book where url in (select bookUrl from BookSourceRecord) order by name")
    fun getBooksInRecord(): Flowable<List<Book>>

    @Query("select url from Book where name=:name and author=:author")
    fun getBookSource(name: String, author: String): List<String>

    @Query("SELECT * FROM Book WHERE url = (select bookUrl from BookSourceRecord where bookName=:name and author=:author)")
    fun getBookInBookSource(name: String, author: String): Flowable<Book>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateBook(book: Book)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertChapters(book: List<Chapter>)

    @Query("select * from Chapter where bookUrl=:bookUrl order by `index`")
    fun getChapters(bookUrl: String): DataSource.Factory<Int, Chapter>

    @Query("select * from Chapter where url=:url")
    fun getChapter(url: String): Chapter

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateChapter(chapter: Chapter)

    @Query("delete from BookSourceRecord where bookName=:bookName and author=:author")
    fun deleteBook(bookName: String, author: String): Int

    @Query("update BookSourceRecord set bookUrl=:url where bookName=:name and author=:author")
    fun updateBookSource(name: String, author: String, url: String): Int

    @Query("select readIndex from BookSourceRecord where bookName=:name and author=:author")
    fun getReadIndex(name: String, author: String): Flowable<Int>

    @Query("update BookSourceRecord set readIndex=:index where bookName=:name and author=:author")
    fun setReadIndex(name: String, author: String, index: Int): Int

    @Query("select * from Chapter where bookUrl=:bookUrl order by `index` desc limit 1")
    fun getLatestChapter(bookUrl: String): Chapter

    @Query("select url,bookUrl,`index`,chapterName,isLoadSuccess from Chapter where bookUrl=:bookUrl and isLoadSuccess=0 order by `index`")
    fun getUnLoadChapters(bookUrl: String): List<Chapter>

    @Query("select url,bookUrl,`index`,chapterName,isLoadSuccess from Chapter where bookUrl=:bookUrl order by `index`")
    fun getChapterIntro(bookUrl: String): Flowable<List<Chapter>>

}


@Database(entities = [Book::class, BookSourceRecord::class, Chapter::class], version = 2)
abstract class BooksDataBase : RoomDatabase() {
    abstract fun bookDao(): BookDao
}

val booksDataBase by lazy {
    Room.databaseBuilder(App.app, BooksDataBase::class.java, "books.db").fallbackToDestructiveMigration()
            .build()
}
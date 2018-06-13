package sjj.fiction.data.source.local

import android.arch.persistence.room.*
import sjj.fiction.model.Book
import sjj.fiction.model.BookGroup
import sjj.fiction.model.Chapter


@Dao
interface BookDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveBookGroups(book: List<BookGroup>)

    @Query("SELECT * FROM BookGroup WHERE bookName=:name and author=:author")
    fun getBookGroup(name: String, author: String): BookGroup

    @Query("delete  from BookGroup where bookName=:name and author=:author")
    fun deleteBookGroup(name: String, author: String)

    @Delete
    fun deleteBookGroup(book: BookGroup)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveBooks(book: List<Book>)

    @Delete
    fun deleteBooks(book: List<Book>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveChapter(book: List<Chapter>)

    @Query("SELECT * FROM Book WHERE id=:id")
    fun getBook(id: String): Book

    @Query("SELECT * FROM Book WHERE name=:name and author=:author")
    fun getBook(name: String, author: String): List<Book>


    @Query("SELECT url,bookId,`index`,chapterName,isLoadSuccess FROM Chapter WHERE bookId=:bookId order by `index`")
    fun getChapterIntro(bookId: String): List<Chapter>

    @Query("SELECT * FROM Chapter WHERE url=:url")
    fun getChapter(url: String): Chapter

    @Delete
    fun deleteChapter(chapter: List<Chapter>)

    @Query("SELECT * FROM BookGroup")
    fun getAllBookGroup(): List<BookGroup>

}
package sjj.novel.data.source.local

import android.arch.persistence.room.*
import io.reactivex.Flowable
import io.reactivex.Observable
import sjj.novel.data.source.remote.rule.BookParseRule

@Dao
interface NovelSourceDao {

    @Query("select * from BookParseRule")
    fun getAllBookParseRule(): Flowable<List<BookParseRule>>

    @Query("select * from BookParseRule where topLevelDomain = :tld")
    fun getBookParseRule(tld: String): BookParseRule

    @Query("select BookParseRule.* from BookParseRule, Book where instr(Book.url,BookParseRule.topLevelDomain) != 0 and Book.name=:bookName and Book.author=:author")
    fun getBookParseRule(bookName:String,author:String):List<BookParseRule>

    @Insert
    fun insertBookParseRule(rule: BookParseRule)

    @Update
    fun updateBookParseRule(rule: BookParseRule)
    @Delete
    fun deleteBookParseRule(rule: BookParseRule)
}
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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveBookParseRule(rule: BookParseRule)

    @Delete
    fun deleteBookParseRule(rule: BookParseRule)
}
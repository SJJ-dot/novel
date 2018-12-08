package sjj.novel.data.source.local

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import io.reactivex.Flowable
import sjj.novel.data.source.remote.rule.BookParseRule

@Dao
interface NovelSourceDao {

    @Query("select * from BookParseRule")
    fun getAllBookParseRule(): Flowable<List<BookParseRule>>


    @Insert
    fun saveBookParseRule(rule: BookParseRule)

    @Delete
    fun deleteBookParseRule(rule: BookParseRule)
}
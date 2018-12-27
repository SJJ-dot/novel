package sjj.novel.data.source.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import io.reactivex.Flowable
import sjj.novel.model.SearchHistory

@Dao
interface SearchHistoryDao {

    /**
     * 获取搜索历史列表
     */
    @Query("select * from SearchHistory order by `id` desc")
    fun getSearchHistory(): Flowable<List<SearchHistory>>

    /**
     * 添加一个搜索纪录
     */
    @Insert(onConflict = REPLACE)
    fun addSearchHistory(searchHistory: SearchHistory)

    /**
     * 删除一个搜索纪录
     */
    @Delete
    fun deleteSearchHistory(searchHistory: List<SearchHistory>): Int

}
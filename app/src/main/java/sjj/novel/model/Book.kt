package sjj.novel.model

import android.arch.persistence.room.*
import android.arch.persistence.room.ForeignKey.CASCADE

/**
 * Created by SJJ on 2017/10/7.
 */
@Entity(indices = [(Index("name", "author"))], foreignKeys = [(ForeignKey(entity = BookSourceRecord::class, parentColumns = ["bookName", "author"], childColumns = ["name", "author"], onDelete = CASCADE))])
data class Book(
        @PrimaryKey
        var url: String = "",
        var name: String = "",
        var author: String = "",
        var bookCoverImgUrl: String = "",
        var intro: String = "",
        var chapterListUrl: String = "",
        @Ignore
        var chapterList: List<Chapter> = mutableListOf(),

        var loadStatus: LoadState = LoadState.Loaded,
        /**
         * 阅读的章节
         */
        @Ignore
        var index: Int = 0,
        @Ignore
        var isThrough:Boolean = false
){
        enum class LoadState{
                UnLoad,Loading,Loaded,LoadFailed
        }
}
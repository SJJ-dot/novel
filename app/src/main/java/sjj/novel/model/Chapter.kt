package sjj.novel.model

import androidx.room.*

/**
 * Created by SJJ on 2017/10/10.
 */
@Entity(indices = [Index("url"), Index("bookUrl")], foreignKeys = [(ForeignKey(entity = Book::class, parentColumns = ["url"], childColumns = ["bookUrl"], onDelete = ForeignKey.CASCADE))])
data class Chapter(
        @PrimaryKey
        var url: String = "",
        var bookUrl: String = "",
        var index: Int = 0,
        var chapterName: String = "",
        var content: String? = "",
        var isLoadSuccess: Boolean = false
)
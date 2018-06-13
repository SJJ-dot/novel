package sjj.fiction.model

import android.arch.persistence.room.*
import java.io.Serializable

/**
 * Created by SJJ on 2017/10/10.
 */
@Entity(indices = [Index("url"), Index("bookId")], foreignKeys = [(ForeignKey(entity = Book::class, parentColumns = ["id"], childColumns = ["bookId"], onDelete = ForeignKey.CASCADE))])
data class Chapter(
        @PrimaryKey var url: String = "",
        var bookId: String = "",
        var index: Int = 0,
        var chapterName: String = "",
        var content: String? = "",
        var isLoadSuccess: Boolean = false
) : Serializable {
    @Ignore
    var isLoading = false
}
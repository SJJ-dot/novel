package sjj.fiction.model

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.ForeignKey
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import sjj.fiction.BookDataBase
import java.io.Serializable

/**
 * Created by SJJ on 2017/10/10.
 */
@Table(database = BookDataBase::class)
data class Chapter(
        @PrimaryKey var url: String = "",
        @Column var bookId: String = "",
        @Column var index: Int = 0,
        @Column var chapterName: String = "",
        var content: ChapterContent = ChapterContent.def,
        @Column var isLoadSuccess: Boolean = false
) : Serializable {
    var isLoading = false
}
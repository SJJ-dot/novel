package sjj.fiction.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import java.io.Serializable

/**
 * Created by SJJ on 2017/10/10.
 */
@Entity
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
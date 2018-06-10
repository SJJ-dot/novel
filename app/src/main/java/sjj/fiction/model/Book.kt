package sjj.fiction.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import java.io.Serializable

/**
 * Created by SJJ on 2017/10/7.
 */
@Entity
data class Book(
        var url: String = "",
        var name: String = "",
        var author: String = "",
        var bookCoverImgUrl: String = "",
        var intro: String = "",
        var chapterListUrl: String = "",
        @Ignore
        var chapterList: List<Chapter> = mutableListOf()
) : Serializable {
    companion object {
        val def = Book()
    }

    @PrimaryKey
    var id = url
}
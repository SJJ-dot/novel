package sjj.fiction.model

import android.arch.persistence.room.*
import android.arch.persistence.room.ForeignKey.CASCADE
import java.io.Serializable

/**
 * Created by SJJ on 2017/10/7.
 */
@Entity(indices = [(Index("name", "author"))],foreignKeys = [(ForeignKey(entity = BookGroup::class, parentColumns = ["bookName", "author"], childColumns = ["name", "author"],onDelete = CASCADE))])
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
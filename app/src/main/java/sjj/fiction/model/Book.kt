package sjj.fiction.model

import com.raizlabs.android.dbflow.annotation.*
import com.raizlabs.android.dbflow.kotlinextensions.oneToMany
import com.raizlabs.android.dbflow.kotlinextensions.select
import org.jetbrains.annotations.PropertyKey
import sjj.fiction.BookDataBase
import java.io.Serializable
import java.util.*
import kotlin.reflect.KProperty

/**
 * Created by SJJ on 2017/10/7.
 */
@Table(database = BookDataBase::class)
data class Book(
        @Column var url: String = "",
        @Column var name: String = "",
        @Column var author: String = "",
        @Column var bookCoverImgUrl: String = "",
        @Column var intro: String = "",
        @Column var chapterListUrl: String = "",
        var chapterList: List<Chapter> = mutableListOf()
) : Serializable {
    companion object {
        val def = Book()
    }

    @PrimaryKey
    var id = url
}
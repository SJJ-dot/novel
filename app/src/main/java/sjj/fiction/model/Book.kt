package sjj.fiction.model

import java.io.Serializable
import kotlin.reflect.KProperty

/**
 * Created by SJJ on 2017/10/7.
 */
data class Book(
        var url: Url = Url.def,
        var name: String = "",
        var author: String = "",
        var bookCoverImg: Url = Url.def,
        var intro: String = "",
        var chapterListUrl: Url = Url.def,
        var chapterList: List<Chapter> = mutableListOf()) : Serializable {
    companion object {
        val def = Book()
    }
}
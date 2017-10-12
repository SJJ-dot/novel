package sjj.fiction.model

import java.io.Serializable

/**
 * Created by SJJ on 2017/10/7.
 */
data class Book(val name: String, val author: String, val intro: String = "", var content: BookContent) : Serializable {
    val originUrls = mutableListOf<Url>(content.bookOrigin)
}
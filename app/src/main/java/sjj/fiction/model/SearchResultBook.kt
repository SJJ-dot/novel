package sjj.fiction.model

import java.io.Serializable

/**
 * Created by SJJ on 2017/10/8.
 */
class SearchResultBook(val name: String = "", val author: String = "", val url: Url) : Serializable {
    val origins: MutableList<Url> = mutableListOf(url)
    override fun toString(): String {
        return "SearchResultBook(name='$name', author='$author', url=$url, origins=$origins)"
    }

}
package sjj.fiction.model

import java.io.Serializable

/**
 * Created by SJJ on 2017/10/8.
 */
data class SearchResultBook(val name: String, val url: Url, val author: String) : Serializable
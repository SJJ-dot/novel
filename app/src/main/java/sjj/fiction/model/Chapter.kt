package sjj.fiction.model

import java.io.Serializable

/**
 * Created by SJJ on 2017/10/10.
 */
data class Chapter(var chapterName: String = "", var url: Url =Url.def, var content: String = "") : Serializable
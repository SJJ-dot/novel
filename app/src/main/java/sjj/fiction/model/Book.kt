package sjj.fiction.model

import java.io.Serializable

/**
 * Created by SJJ on 2017/10/7.
 */
data class Book(val name: String, val author: String, val coverImgUrl: Url, val intro: String, val latestChapter: Chapter, val chapterList: List<Chapter>) : Serializable {
    val originUrls = mutableSetOf<Url>()
}
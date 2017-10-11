package sjj.fiction.model

import java.io.Serializable

/**
 * Created by SJJ on 2017/10/7.
 */
data class Book(val name: String, val author: String, val intro: String, var currentOriginUrl: Url, val coverImgUrl: Url, var latestChapter: Chapter, var chapterList: List<Chapter> = mutableListOf()) : Serializable {
    val originUrls = mutableListOf<Url>()
    var originChapterList: Url = currentOriginUrl
}
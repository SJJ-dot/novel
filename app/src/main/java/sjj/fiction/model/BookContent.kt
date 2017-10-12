package sjj.fiction.model

import java.io.Serializable

/**
 * Created by SJJ on 2017/10/12.
 */

data class BookContent(var bookOrigin: Url, var bookCoverImg: Url, var latestChapter: Chapter, var chapterListOrigin: Url, var chapterList: List<Chapter> = mutableListOf()) : Serializable
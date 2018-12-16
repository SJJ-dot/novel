package sjj.novel.model

import android.arch.persistence.room.*
import android.arch.persistence.room.ForeignKey.CASCADE
import sjj.novel.data.repository.NovelSourceRepository
import sjj.novel.data.source.remote.rule.BookParseRule
import java.util.*
import java.util.zip.CRC32

/**
 * Created by SJJ on 2017/10/7.
 */
@Entity(indices = [(Index("name", "author"))], foreignKeys = [(ForeignKey(entity = BookSourceRecord::class, parentColumns = ["bookName", "author"], childColumns = ["name", "author"], onDelete = CASCADE))])
data class Book(
        @PrimaryKey
        var url: String = "",
        var name: String = "",
        var author: String = "",
        var bookCoverImgUrl: String = "",
        var intro: String = "",
        var chapterListUrl: String = "",
        @Ignore
        var chapterList: List<Chapter> = mutableListOf(),

        var loadStatus: LoadState = LoadState.UnLoad,
        /**
         * 阅读的章节
         */
        @Ignore
        var index: Int = 0,
        @Ignore
        var readChapterName: String = "",
        @Ignore
        var isThrough: Boolean = false,

        @Ignore
        var origin: BookParseRule? = null
) {
    @Ignore
    var lastChapter: Chapter? = null
        get() {
            return field ?: chapterList.lastOrNull()
        }

    override fun equals(other: Any?): Boolean {
        if (other is Book && other.url == url) {
            return true
        }
        return false
    }

    override fun hashCode(): Int {
        return Objects.hashCode(url)
    }

    fun id(): Long {
        val crC32 = CRC32()
        crC32.update(url.toByteArray())
       return url.hashCode().toLong() shl 32 or crC32.value
    }
    /**
     * 书籍详情加载状态
     */
    enum class LoadState {
        /**
         * 搜索结果未加载详情
         */
        UnLoad,
        /**
         * 正在加载书籍详情
         */
        Loading,
        /**
         * 加载成功
         */
        Loaded,
        /**
         * 加载失败
         */
        LoadFailed
    }

}
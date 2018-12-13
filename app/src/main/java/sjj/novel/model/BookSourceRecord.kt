package sjj.novel.model

import android.arch.persistence.room.Entity

/**
 * Created by SJJ on 2017/10/14.
 */
@Entity(primaryKeys = ["bookName", "author"])
class BookSourceRecord {
    var bookName = ""
    var author = ""
    var bookUrl = ""
    var readIndex: Int = 0
    var chapterName: String = ""
    /**
     * 是否已经读完
     */
    var isThrough: Boolean = false

    override fun toString(): String {
        return "BookSourceRecord(bookName='$bookName', author='$author', bookUrl='$bookUrl', readIndex=$readIndex)"
    }
}


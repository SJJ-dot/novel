package sjj.fiction.model

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import sjj.fiction.BookDataBase

/**
 * Created by SJJ on 2017/10/15.
 */
@Table(database = BookDataBase::class)
data class ChapterContent(@PrimaryKey var url: String = "", @Column var content: String = "")
package sjj.novel.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import org.jetbrains.anko.db.UNIQUE

@Entity(indices = arrayOf(Index("content",unique = true)))
class SearchHistory(
        @PrimaryKey(autoGenerate = true)
        var id: Int = 0,
        var content: String = ""
)
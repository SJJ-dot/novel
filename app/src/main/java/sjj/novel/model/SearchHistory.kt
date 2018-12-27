package sjj.novel.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import org.jetbrains.anko.db.UNIQUE

@Entity(indices = arrayOf(Index("content",unique = true)))
class SearchHistory(
        @PrimaryKey(autoGenerate = true)
        var id: Int = 0,
        var content: String = ""
)
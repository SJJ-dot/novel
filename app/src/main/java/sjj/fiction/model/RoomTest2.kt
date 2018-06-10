package sjj.fiction.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.PrimaryKey

@Entity(foreignKeys =[(ForeignKey(entity = RoomTest::class,parentColumns = ["uid"],childColumns = ["pid"]))] )
class RoomTest2 {
    @PrimaryKey(autoGenerate = true)
    var id = 0
    var pid = 1
    var des = "des"
}
package sjj.fiction.model

import android.arch.persistence.room.*
import sjj.fiction.data.repository.Converters

@Entity
class RoomTest {
    @PrimaryKey(autoGenerate = true)
    var uid = 0;

    @ColumnInfo(name = "first_name")
    var firstName = "firstName"

    var lastName = "last_name"
    @TypeConverters(value = [Converters::class])
    var roomTest2s: List<RoomTest2>? = null

    override fun toString(): String {
        return "RoomTest(uid=$uid, firstName='$firstName', lastName='$lastName', roomTest2s=$roomTest2s)"
    }

}
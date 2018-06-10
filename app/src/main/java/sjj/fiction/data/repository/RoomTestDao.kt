package sjj.fiction.data.repository

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import sjj.fiction.model.RoomTest
import sjj.fiction.model.RoomTest2


@Dao
interface RoomTestDao {
    @get:Query("SELECT * FROM RoomTest")
    val all: List<RoomTest>

    @Query("SELECT * FROM RoomTest WHERE uid IN (:userIds)")
    fun loadAllByIds(userIds: IntArray): List<RoomTest>

    @Query("SELECT * FROM RoomTest WHERE first_name LIKE :first AND " + "lastName LIKE :last LIMIT 1")
    fun findByName(first: String, last: String): RoomTest

    @Insert
    fun insertAll(vararg users: RoomTest)

    @Delete
    fun delete(user: RoomTest)

    @Insert
    fun insert(vararg roomTest: RoomTest2)

    @Query("select * from RoomTest2")
    fun allR2():List<RoomTest2>

}
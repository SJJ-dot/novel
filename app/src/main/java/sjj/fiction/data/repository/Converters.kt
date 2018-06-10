package sjj.fiction.data.repository

import android.arch.persistence.room.TypeConverter
import sjj.fiction.model.RoomTest2
import sjj.fiction.util.fromJson
import sjj.fiction.util.gson


class Converters {
    @TypeConverter
    fun stringToList(value: String?): List<RoomTest2>? {
        return if (value == null) null else gson.fromJson(value)
    }

    @TypeConverter
    fun listToString(date: List<RoomTest2>?): String? {
        return if (date == null) null else gson.toJson(date)
    }
}
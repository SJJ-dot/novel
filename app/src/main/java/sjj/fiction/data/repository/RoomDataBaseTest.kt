package sjj.fiction.data.repository

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.migration.Migration
import sjj.fiction.App
import sjj.fiction.model.RoomTest
import sjj.fiction.model.RoomTest2


@Database(entities = [RoomTest::class,RoomTest2::class], version = 2)
abstract class RoomDataBaseTest : RoomDatabase() {
    abstract fun userDao(): RoomTestDao
}

val roomDataBaseTest by lazy { Room.databaseBuilder(App.app, RoomDataBaseTest::class.java, "database-name.db").addMigrations(object : Migration(1,2){
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("""
            CREATE TABLE `RoomTest2`
            (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
             `pid` INTEGER NOT NULL,
             `des` TEXT NOT NULL,
             FOREIGN KEY(`pid`) REFERENCES `RoomTest`(`uid`) ON UPDATE NO ACTION ON DELETE NO ACTION )
             """.trimIndent())
    }
}).build() }

@Database(entities = [RoomTest::class,RoomTest2::class], version = 2)
abstract class RoomDataBaseTestMemory : RoomDatabase() {
    abstract fun userDao(): RoomTestDao
}

val roomDataBaseTestMemory by lazy { Room.inMemoryDatabaseBuilder(App.app, RoomDataBaseTestMemory::class.java).build() }
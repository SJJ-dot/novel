package sjj.fiction.data.source.local

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import sjj.fiction.App
import sjj.fiction.model.Book
import sjj.fiction.model.BookGroup
import sjj.fiction.model.Chapter

@Database(entities = [Book::class, BookGroup::class, Chapter::class], version = 1)
abstract class BooksDataBase : RoomDatabase() {
    abstract fun bookDao(): BookDao
}

val booksDataBase by lazy {
    Room.databaseBuilder(App.app, BooksDataBase::class.java, "books.db").fallbackToDestructiveMigration()
//            .addMigrations(object : Migration(1, 2) {
//                override fun migrate(database: SupportSQLiteDatabase) {
//                    database.execSQL("""
//            CREATE TABLE `RoomTest2`
//            (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
//             `pid` INTEGER NOT NULL,
//             `des` TEXT NOT NULL,
//             FOREIGN KEY(`pid`) REFERENCES `RoomTest`(`uid`) ON UPDATE NO ACTION ON DELETE NO ACTION )
//             """.trimIndent())
//                }
//            })
            .build()
}
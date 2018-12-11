package sjj.novel.data.source.local

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.arch.persistence.room.migration.Migration
import sjj.novel.Session
import sjj.novel.data.source.remote.rule.BookParseRule
import sjj.novel.model.Book
import sjj.novel.model.BookSourceRecord
import sjj.novel.model.Chapter


@Database(entities = [Book::class, BookSourceRecord::class, Chapter::class, BookParseRule::class], version = 2)
@TypeConverters(Converters::class)
abstract class BooksDataBase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun novelSourceDao(): NovelSourceDao
}

val booksDataBase by lazy {
    Room.databaseBuilder(Session.ctx, BooksDataBase::class.java, "books.db")
//            .addMigrations(object : Migration(2, 3) {
//                override fun migrate(database: SupportSQLiteDatabase) {
//                    database.execSQL("PRAGMA foreign_keys=OFF")
//                    database.execSQL("CREATE TABLE `book_temp` (`url` TEXT NOT NULL, `name` TEXT NOT NULL, `author` TEXT NOT NULL, `bookCoverImgUrl` TEXT NOT NULL, `intro` TEXT NOT NULL, `chapterListUrl` TEXT NOT NULL, `loadStatus` TEXT NOT NULL, PRIMARY KEY(`url`), FOREIGN KEY(`name`, `author`) REFERENCES `BookSourceRecord`(`bookName`, `author`) ON UPDATE NO ACTION ON DELETE CASCADE )")
//                    database.execSQL("INSERT INTO 'book_temp'('url','name','author','bookCoverImgUrl','intro','chapterListUrl','loadStatus') SELECT 'url','name','author','bookCoverImgUrl','intro','chapterListUrl','loadStatus' FROM 'Book'")
//                    database.execSQL("DROP TABLE 'Book'")
//                    database.execSQL("ALTER TABLE 'book_temp' RENAME TO 'Book'")
//                    database.execSQL("PRAGMA foreign_keys=ON")
//                }
//            })
            .build()
}
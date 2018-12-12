package sjj.novel.data.source.local

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.arch.persistence.room.migration.Migration
import sjj.alog.Log
import sjj.novel.Session
import sjj.novel.data.source.remote.rule.BookParseRule
import sjj.novel.model.Book
import sjj.novel.model.BookSourceRecord
import sjj.novel.model.Chapter


@Database(entities = [Book::class, BookSourceRecord::class, Chapter::class, BookParseRule::class], version = 3)
@TypeConverters(Converters::class)
abstract class BooksDataBase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun novelSourceDao(): NovelSourceDao
}

val booksDataBase by lazy {
    Room.databaseBuilder(Session.ctx, BooksDataBase::class.java, "books.db")
            .addMigrations(object : Migration(2, 3) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("PRAGMA foreign_keys=OFF")
                    database.execSQL("DROP INDEX `index_Book_name_author`")
                    database.execSQL("ALTER TABLE 'Book' RENAME TO 'book_temp'")
                    database.execSQL("CREATE TABLE `Book` (`url` TEXT NOT NULL, `name` TEXT NOT NULL, `author` TEXT NOT NULL, `bookCoverImgUrl` TEXT NOT NULL, `intro` TEXT NOT NULL, `chapterListUrl` TEXT NOT NULL, `loadStatus` TEXT NOT NULL, PRIMARY KEY(`url`), FOREIGN KEY(`name`, `author`) REFERENCES `BookSourceRecord`(`bookName`, `author`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                    database.execSQL("CREATE  INDEX `index_Book_name_author` ON `Book` (`name`, `author`)")

                    val cursor = database.query("select * from book_temp")
                    while (cursor.moveToNext()) {
                        Log.e("url "+cursor.getString(cursor.getColumnIndex("url")))
                        database.execSQL("INSERT INTO 'Book'('url','name','author','bookCoverImgUrl','intro','chapterListUrl','loadStatus') values(?,?,?,?,?,?,?)",
                                arrayOf(cursor.getString(cursor.getColumnIndex("url")),
                                        cursor.getString(cursor.getColumnIndex("name")),
                                        cursor.getString(cursor.getColumnIndex("author")),
                                        cursor.getString(cursor.getColumnIndex("bookCoverImgUrl"))?:"",
                                        cursor.getString(cursor.getColumnIndex("intro"))?:"",
                                        cursor.getString(cursor.getColumnIndex("chapterListUrl"))?:"",
                                        cursor.getString(cursor.getColumnIndex("loadStatus"))))
                    }
                    cursor.close()
                    database.execSQL("DROP TABLE 'book_temp'")
                    database.execSQL("PRAGMA foreign_keys=ON")
                }
            })
            .build()
}
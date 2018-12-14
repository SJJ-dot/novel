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


@Database(entities = [Book::class, BookSourceRecord::class, Chapter::class, BookParseRule::class], version = 5)
@TypeConverters(Converters::class)
abstract class BooksDataBase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun novelSourceDao(): NovelSourceDao
}

val booksDataBase by lazy {
    Room.databaseBuilder(Session.ctx, BooksDataBase::class.java, "books.db")
            .fallbackToDestructiveMigrationFrom(1)//忘记了最开始的数据库格式 也不想回去看
            .addMigrations(object : Migration(2, 3) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("PRAGMA foreign_keys=OFF")
                    database.execSQL("DROP INDEX `index_Book_name_author`")
                    database.execSQL("ALTER TABLE `Book` RENAME TO `book_temp`")
                    database.execSQL("CREATE TABLE `Book` (`url` TEXT NOT NULL, `name` TEXT NOT NULL, `author` TEXT NOT NULL, `bookCoverImgUrl` TEXT NOT NULL, `intro` TEXT NOT NULL, `chapterListUrl` TEXT NOT NULL, `loadStatus` TEXT NOT NULL, PRIMARY KEY(`url`), FOREIGN KEY(`name`, `author`) REFERENCES `BookSourceRecord`(`bookName`, `author`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                    database.execSQL("CREATE  INDEX `index_Book_name_author` ON `Book` (`name`, `author`)")

                    val cursor = database.query("SELECT * FROM `book_temp`")
                    while (cursor.moveToNext()) {
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
            .addMigrations(object : Migration(3, 4) {
                override fun migrate(database: SupportSQLiteDatabase) {
//                    ALTER TABLE 表名 ADD COLUMN 列名 数据类型
                    database.execSQL("ALTER TABLE `BookSourceRecord` ADD COLUMN `isThrough` INTEGER NOT NULL default 0")
                }
            })
            .addMigrations(object : Migration(4, 5) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("ALTER TABLE `BookSourceRecord` ADD COLUMN `chapterName` TEXT NOT NULL default ''")
                }
            })
            .build()
}
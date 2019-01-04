package sjj.novel.data.source.local

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import sjj.alog.Log
import sjj.novel.Session
import sjj.novel.data.source.remote.rule.BookParseRule
import sjj.novel.model.Book
import sjj.novel.model.BookSourceRecord
import sjj.novel.model.Chapter
import sjj.novel.model.SearchHistory


@Database(entities = [Book::class, BookSourceRecord::class, Chapter::class, BookParseRule::class, SearchHistory::class], version = 8, exportSchema = false)
@TypeConverters(Converters::class)
abstract class BooksDataBase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun novelSourceDao(): NovelSourceDao
    abstract fun searchHistoryDao(): SearchHistoryDao
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
                                        cursor.getString(cursor.getColumnIndex("bookCoverImgUrl"))
                                                ?: "",
                                        cursor.getString(cursor.getColumnIndex("intro")) ?: "",
                                        cursor.getString(cursor.getColumnIndex("chapterListUrl"))
                                                ?: "",
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
            .addMigrations(object : Migration(5, 6) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("CREATE TABLE `SearchHistory` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `content` TEXT NOT NULL)")
                    database.execSQL("CREATE UNIQUE INDEX `index_SearchHistory_content` ON `SearchHistory` (`content`)");
                }
            })
            .addMigrations(object : Migration(6, 7) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("ALTER TABLE `BookSourceRecord` ADD COLUMN `pagePos` INTEGER NOT NULL default 0")
                }
            }).addMigrations(object : Migration(7, 8) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("ALTER TABLE `BookSourceRecord` ADD COLUMN `sequence` INTEGER NOT NULL default 0")
                    val cursor = database.query("SELECT `bookUrl` FROM `BookSourceRecord`")
                    var seq = 0
                    while (cursor.moveToNext()) {
                        val args = arrayOf(seq, cursor.getString(cursor.getColumnIndex("bookUrl")))
                        database.execSQL("update BookSourceRecord set sequence=? where bookUrl=?", args)
                        seq++
                    }
                    cursor.close()
                }
            })
            .build()
}
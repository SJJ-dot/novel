package sjj.novel.data.source.local

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
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
            .fallbackToDestructiveMigration()
            .build()
}
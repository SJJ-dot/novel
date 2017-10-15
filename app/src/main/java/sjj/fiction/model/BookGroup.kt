package sjj.fiction.model

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import sjj.alog.Log
import sjj.fiction.BookDataBase
import java.io.Serializable
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.javaMethod

/**
 * Created by SJJ on 2017/10/14.
 */
@Table(database = BookDataBase::class)
data class BookGroup(var currentBook: Book = Book.def, var books: MutableList<Book> = mutableListOf()) : Serializable {
    @PrimaryKey
    var bookName = currentBook.name
    @PrimaryKey
    var author = currentBook.author
    @Column
    var bookId = currentBook.id
    @Column
    var readIndex: Int = 0
}
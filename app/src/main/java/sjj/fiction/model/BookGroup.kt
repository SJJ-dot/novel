package sjj.fiction.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import java.io.Serializable

/**
 * Created by SJJ on 2017/10/14.
 */
@Entity(primaryKeys = ["bookName","author"])
data class BookGroup(@Ignore var currentBook: Book = Book.def,@Ignore  var books: MutableList<Book> = mutableListOf()) : Serializable {
    var bookName = currentBook.name
    var author = currentBook.author
    var bookId = currentBook.id
    var readIndex: Int = 0

    override fun toString(): String {
        return "BookGroup(currentBook=$currentBook, books=$books, bookName='$bookName', author='$author', bookId='$bookId', readIndex=$readIndex)"
    }

}
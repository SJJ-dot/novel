package sjj.fiction.model

import sjj.alog.Log
import java.io.Serializable
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.javaMethod

/**
 * Created by SJJ on 2017/10/14.
 */
data class BookGroup(var currentBook: Book = Book.def, var books: MutableList<Book> = mutableListOf()) : Serializable
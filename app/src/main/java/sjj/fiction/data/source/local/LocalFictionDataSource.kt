package sjj.fiction.data.source.local

import com.google.gson.Gson
import com.raizlabs.android.dbflow.kotlinextensions.*
import io.reactivex.Observable
import sjj.fiction.BookDataBase
import sjj.fiction.data.repository.FictionDataRepository
import sjj.fiction.model.*
import sjj.fiction.util.def
/**
 * Created by SJJ on 2017/10/15.
 */
class LocalFictionDataSource : FictionDataRepository.SourceLocal {

    private val KEY_SEARCH_HISTORY = "LOCAL_FICTION_DATA_SOURCE_KEY_SEARCH_HISTORY"


    private val gson = Gson()

    override fun saveBookGroup(book: List<BookGroup>): Observable<List<BookGroup>> {
        return def {
            database<BookDataBase>().executeTransaction {
                book.forEach { bookG ->
                    bookG.save(it)
                    bookG.books.forEach { b ->
                        b.save(it)
                        b.chapterList.forEach { c -> c.save(it) }
                    }
                }
            }
            book
        }
    }

    override fun updateBookGroup(book: BookGroup): Observable<BookGroup> {
        return def {
            book.update()
            book
        }
    }

    override fun updateBook(book: Book): Observable<Book> {
        return def {
            database<BookDataBase>().executeTransaction {
                book.update(it)
                book.chapterList.forEach { chapter: Chapter ->
                    chapter.save(it)
                }
            }
            book
        }
    }

    override fun saveChapter(chapter: Chapter): Observable<Chapter> {
        return def {
            chapter.update()
            chapter
        }
    }

    override fun loadBookDetailsAndChapter(book: Book): Observable<Book> {
        return def {
            val book1 = (select from Book::class where (Book_Table.id eq book.id)).result ?: throw Exception("Not Found")
            book.url = book1.url
            book.name = book1.name
            book.author = book1.author
            book.bookCoverImgUrl = book1.bookCoverImgUrl
            book.intro = book1.intro
            book.chapterListUrl = book1.chapterListUrl
            val list = (sjj.fiction.util.select(*Chapter.noContent) from Chapter::class where (Chapter_Table.bookId eq book.id)).list
            if (list.isEmpty()) {
                throw Exception("chapter is null")
            }
            book.chapterList = list
            book
        }
    }

    override fun loadBookChapter(chapter: Chapter): Observable<Chapter> = def {
        val chapter1 = (select from Chapter::class where (Chapter_Table.url eq chapter.url)).result ?: throw Exception("not found chapter ${chapter.chapterName}")
        chapter.url = chapter1.url
        chapter.bookId = chapter1.bookId
        chapter.index = chapter1.index
        chapter.chapterName = chapter1.chapterName
        chapter.isLoadSuccess = chapter1.isLoadSuccess
        chapter.content = chapter1.content
        chapter
    }

    override fun loadBookGroups(): Observable<List<BookGroup>> {
        return def {
            val listGroup = mutableListOf<BookGroup>()
            database<BookDataBase>().executeTransaction {
                val list = (select from BookGroup::class).list
                list.forEach {
//                    it.currentBook = (select from Book::class where (Book_Table.id eq it.bookId)).result ?: it.currentBook
                    it.books = (select from Book::class where (Book_Table.name eq it.bookName) and (Book_Table.author eq it.author)).list
                    it.books.forEach {b->
                        b.chapterList = (sjj.fiction.util.select(*Chapter.noContent) from Chapter::class where (Chapter_Table.bookId eq b.id)).list
                        if (it.bookId== b.id) {
                            it.currentBook = b
                        }
                    }
                }
                listGroup.addAll(list)
            }
            return@def listGroup
        }
    }

    override fun loadBookGroup(bookName: String, author: String): Observable<BookGroup> {
        return def {
            val result = (select from BookGroup::class where (BookGroup_Table.bookName eq bookName) and (BookGroup_Table.author eq author)).result!!
//            result.currentBook = (select from Book::class where (Book_Table.id eq result.bookId)).result!!
            result.books = (select from Book::class where (Book_Table.name eq result.bookName) and (Book_Table.author eq result.author)).list
            result.books.forEach {
                it.chapterList = (sjj.fiction.util.select(*Chapter.noContent) from Chapter::class where (Chapter_Table.bookId eq it.id)).list
                if (result.bookId == it.id) {
                    result.currentBook = it
                }
            }
            result
        }
    }

    override fun deleteBookGroup(bookName: String, author: String): Observable<BookGroup> {
        return def {
            val bookGroup = (select from BookGroup::class where (BookGroup_Table.bookName eq bookName) and (BookGroup_Table.author eq author)).result ?: throw Exception("not found book $bookName")
            val list = (select from Book::class where (Book_Table.name eq bookName) and (Book_Table.author eq author)).list
            val chapter = list.flatMap {
                (sjj.fiction.util.select(*Chapter.noContent) from Chapter::class where (Chapter_Table.bookId eq it.id)).list
            }
            database<BookDataBase>().executeTransaction { wd ->
                chapter.forEach { it.delete(wd) }
                list.forEach { it.delete(wd) }
                bookGroup.delete(wd)
            }
            bookGroup
        }
    }

}
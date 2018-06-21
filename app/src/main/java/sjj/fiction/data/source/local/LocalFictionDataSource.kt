package sjj.fiction.data.source.local

import com.google.gson.Gson
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import sjj.fiction.data.repository.FictionDataRepository
import sjj.fiction.model.Book
import sjj.fiction.model.BookGroup
import sjj.fiction.model.BookSourceRecord
import sjj.fiction.model.Chapter
import sjj.fiction.util.def

/**
 * Created by SJJ on 2017/10/15.
 */
class LocalFictionDataSource: FictionDataRepository.LocalSource {


    private val bookDao by lazy { booksDataBase.bookDao() }

    override fun saveBooks(books: List<Pair<BookSourceRecord, List<Book>>>): Single<List<Book>> {
        return Single.fromCallable {
            booksDataBase.runInTransaction {

            }
            books.map {pair->
                pair.second.find { pair.first.bookUrl == it.url }!!
            }
        }
    }

    override fun saveBookGroup(book: List<BookGroup>): Observable<List<BookGroup>> {
        return def {
            with(booksDataBase.bookDao()) {
                saveBookGroups(book)
                book.forEach {
                    saveBooks(it.books)
                    it.books.forEach {
                        saveChapter(it.chapterList)
                    }
                }
            }
            book
        }
    }

    override fun updateBookGroup(book: BookGroup): Observable<BookGroup> {
        return def {
            booksDataBase.bookDao().saveBookGroups(listOf(book))
            book
        }
    }

    override fun updateBook(book: Book): Observable<Book> {
        return def {
            booksDataBase.bookDao().saveBooks(listOf(book))
            booksDataBase.bookDao().saveChapter(book.chapterList)
            book
        }
    }

    override fun saveChapter(chapter: Chapter): Observable<Chapter> {
        return def {
            booksDataBase.bookDao().saveChapter(listOf(chapter))
            chapter
        }
    }

    override fun loadBookDetailsAndChapter(book: Book): Observable<Book> {
        return def {
            val r = booksDataBase.bookDao().getBook(book.id)
            r.chapterList = booksDataBase.bookDao().getChapterIntro(book.id)
            book.url = r.url
            book.name = r.name
            book.author = r.author
            book.bookCoverImgUrl = r.bookCoverImgUrl
            book.intro = r.intro
            book.chapterList = r.chapterList
            book
        }
    }

    override fun loadBookChapter(chapter: Chapter): Observable<Chapter> = def {
        val chapter1 = booksDataBase.bookDao().getChapter(chapter.url)
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
            val group = booksDataBase.bookDao().getAllBookGroup()
            group.forEach {
                it.books = booksDataBase.bookDao().getBook(it.bookName, it.author).toMutableList()
                it.books.forEach { b ->
                    b.chapterList = booksDataBase.bookDao().getChapterIntro(b.id)
                    if (it.bookId == b.id) {
                        it.currentBook = b
                    }
                }
            }
            return@def group
        }
    }

    override fun loadBookGroup(bookName: String, author: String): Observable<BookGroup> {
        return def {
            val result = booksDataBase.bookDao().getBookGroup(bookName, author)
//            result.currentBook = (select from Book::class where (Book_Table.id eq result.bookId)).result!!
            result.books = booksDataBase.bookDao().getBook(bookName, author).toMutableList()
            result.books.forEach { b ->
                b.chapterList = booksDataBase.bookDao().getChapterIntro(b.id)
                if (result.bookId == b.id) {
                    result.currentBook = b
                }
            }
            result
        }
    }

    override fun deleteBookGroup(bookName: String, author: String): Observable<Int> {
        return Observable.fromCallable<Int> {
            booksDataBase.bookDao().deleteBookGroup(bookName, author)
        }.subscribeOn(Schedulers.io())
    }

}
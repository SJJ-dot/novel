package sjj.fiction.data.Repository.impl

import io.reactivex.Observable
import sjj.alog.Log
import sjj.fiction.data.Repository.FictionDataRepository
import sjj.fiction.data.source.remote.dhzw.DhzwDataSource
import sjj.fiction.data.source.remote.yunlaige.YunlaigeDataSource
import sjj.fiction.model.Book
import sjj.fiction.model.BookGroup
import sjj.fiction.model.Chapter
import sjj.fiction.model.Url

/**
 * Created by SJJ on 2017/9/3.
 */
class FictionDataRepositoryImpl : FictionDataRepository {
    private var sources = mutableMapOf<Url, FictionDataRepository.Source>()

    init {
        val dh: FictionDataRepository.Source = DhzwDataSource()
        sources[dh.domain()] = dh
        val yu: FictionDataRepository.Source = YunlaigeDataSource()
        sources[yu.domain()] = yu
    }

    override fun search(search: String): Observable<List<BookGroup>> {
        return Observable.combineLatest(sources.map { it.value.search(search) }) { t ->
            val list = mutableListOf<BookGroup>()
            t.toList().forEach { s ->
                s as List<Book>
                s.forEach {
                    val find = list.find { r -> r.currentBook.name == it.name && r.currentBook.author == it.author }
                    if (find == null) {
                        list.add(BookGroup(it, mutableListOf(it)))
                    } else {
                        find.books.add(it)
                    }
                }
            }
            list
        }
    }

    override fun loadBookDetailsAndChapter(book: BookGroup): Observable<BookGroup> = sources[book.currentBook.url.domain()]?.loadBookDetailsAndChapter(book.currentBook)?.map { book } ?: error("未知源 ${book.currentBook.url}")

    override fun loadBookChapter(chapter: Chapter): Observable<Chapter> = sources[chapter.url.domain()]?.loadBookChapter(chapter) ?: error("未知源 ${chapter.url}")
}
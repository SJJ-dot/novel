package sjj.novel.details

import android.arch.lifecycle.ViewModel
import io.reactivex.Observable
import sjj.novel.data.repository.novelDataRepository
import sjj.novel.model.Book
import sjj.novel.model.Chapter

class DetailsViewModel(val name: String, val author: String) : ViewModel() {
    val book = novelDataRepository.getBookInBookSource(name, author)

    val bookSource = novelDataRepository.getBookSource(name, author)

    fun setBookSource(url: String): Observable<Int> {
        return novelDataRepository.setBookSource(name, author, url)
    }

//    fun getLatestChapter(url: String): Observable<Chapter> {
//        return novelDataRepository.getLatestChapter(url)
//    }

    fun getChapters(bookUrl: String) = novelDataRepository.getChapters(bookUrl)

    val bookSourceRecord = novelDataRepository.getBookSourceRecord(name, author)

    fun setReadIndex(index: Chapter): Observable<Int> {
        return novelDataRepository.setReadIndex(name, author, index)
    }

    fun refresh(it: Book): Observable<Book> {
       return novelDataRepository.refreshBook(it.url)
    }
}
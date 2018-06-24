package sjj.fiction.details

import android.arch.lifecycle.ViewModel
import io.reactivex.Flowable
import io.reactivex.Observable
import sjj.fiction.data.repository.fictionDataRepository
import sjj.fiction.model.Book
import sjj.fiction.model.Chapter

class DetailsViewModel(val name: String, val author: String) : ViewModel() {
    val book = fictionDataRepository.getBookInBookSource(name, author)

    val bookSource = fictionDataRepository.getBookSource(name, author)

    fun setBookSource(url: String): Observable<Int> {
        return fictionDataRepository.setBookSource(name, author, url)
    }

//    fun getLatestChapter(url: String): Observable<Chapter> {
//        return fictionDataRepository.getLatestChapter(url)
//    }

    fun getChapters(bookUrl: String) = fictionDataRepository.getChapters(bookUrl)

    val readIndex = fictionDataRepository.getReadIndex(name, author)

    fun setReadIndex(index: Int): Observable<Int> {
        return fictionDataRepository.setReadIndex(name, author, index)
    }

    fun refresh(it: Book) {
        fictionDataRepository.refreshBook(it.url).subscribe()
    }
}
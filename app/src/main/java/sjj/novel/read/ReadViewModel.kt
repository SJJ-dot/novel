package sjj.novel.read

import android.arch.lifecycle.ViewModel
import io.reactivex.Observable
import sjj.novel.data.repository.novelDataRepository
import sjj.novel.model.Chapter

class ReadViewModel(val name: String, val author: String) : ViewModel() {

    val book = novelDataRepository.getBookInBookSource(name, author)

    private var lastReadIndex = 0
    private var isThrough: Boolean? = null

    fun getChapters(bookUrl: String) = novelDataRepository.getChapters(bookUrl)

    fun loadChapter(chapter: Chapter): Observable<Chapter> {
        return novelDataRepository.loadChapter(chapter)
    }

    fun getChapter(url: String): Observable<Chapter> {
        return novelDataRepository.getChapter(url)
    }

    val readIndex = novelDataRepository.getBookSourceRecord(name, author).doOnNext {
        lastReadIndex = it.readIndex
    }

    fun setReadIndex(index: Int, isThrough: Boolean = false): Observable<Int> {
        if (lastReadIndex == index && this.isThrough == isThrough) {
            return Observable.empty()
        }
        this.isThrough = isThrough
        lastReadIndex = index
        return novelDataRepository.setReadIndex(name, author, index, isThrough)
    }


    fun cachedBookChapter(bookUrl: String) = novelDataRepository.cachedBookChapter(bookUrl)

}
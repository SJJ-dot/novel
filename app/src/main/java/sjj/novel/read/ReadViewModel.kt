package sjj.novel.read

import android.arch.lifecycle.ViewModel
import io.reactivex.Observable
import sjj.novel.data.repository.novelDataRepository
import sjj.novel.model.Chapter

class ReadViewModel(val name: String, val author: String) : ViewModel() {

    val book = novelDataRepository.getBookInBookSource(name, author)

    private var lastReadIndex = 0

    fun getChapters(bookUrl: String) = novelDataRepository.getChapters(bookUrl)

    fun loadChapter(chapter: Chapter): Observable<Chapter> {
        return novelDataRepository.loadChapter(chapter)
    }

    fun getChapter(url: String): Observable<Chapter> {
        return novelDataRepository.getChapter(url)
    }

    val readIndex = novelDataRepository.getReadIndex(name, author).doOnNext {
        lastReadIndex = it
    }

    fun setReadIndex(index: Int): Observable<Int> {
        if (lastReadIndex == index) {
            return Observable.empty()
        }
        lastReadIndex = index
        return novelDataRepository.setReadIndex(name, author, index)
    }


    fun cachedBookChapter(bookUrl: String) = novelDataRepository.cachedBookChapter(bookUrl)

}
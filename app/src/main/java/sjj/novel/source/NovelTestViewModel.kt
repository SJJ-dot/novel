package sjj.novel.source

import android.arch.lifecycle.ViewModel
import io.reactivex.Observable
import sjj.novel.data.repository.novelSourceRepository
import sjj.novel.data.source.remote.CommonNovelEngine
import sjj.novel.model.Book
import sjj.novel.model.Chapter


class NovelTestViewModel(val topLevelDomain: String?) : ViewModel() {
    private val novelEngine = Observable.fromCallable {
        topLevelDomain
    }.flatMap { key ->
        novelSourceRepository.getBookParseRule(key)
                .map {
                    CommonNovelEngine(it)
                }
    }


    fun search(key: String): Observable<List<Book>> {
        return novelEngine.flatMap {
            it.search(key)
        }
    }

    fun getBook(url: String): Observable<Book> {
        return novelEngine.flatMap {
            it.getBook(url)
        }
    }

    fun getChapterContent(chapter: Chapter): Observable<Chapter> {
        return novelEngine.flatMap {
            it.getChapterContent(chapter)
        }
    }
}
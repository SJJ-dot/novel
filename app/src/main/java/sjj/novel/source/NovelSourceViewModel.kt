package sjj.novel.source

import android.arch.lifecycle.ViewModel
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import sjj.novel.data.repository.novelSourceRepository
import sjj.novel.data.source.remote.rule.BookParseRule

class NovelSourceViewModel : ViewModel() {
    fun getAllBookParseRule() = novelSourceRepository.getAllBookParseRule()

    fun saveBookParseRule(rule: BookParseRule): Observable<BookParseRule> {
        return novelSourceRepository.saveBookParseRule(rule)
    }

    fun deleteBookParseRule(rule: BookParseRule): Observable<BookParseRule> {
        return novelSourceRepository.deleteBookParseRule(rule)
    }
}
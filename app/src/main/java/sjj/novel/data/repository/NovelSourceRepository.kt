package sjj.novel.data.repository

import io.reactivex.Observable
import sjj.novel.data.source.remote.rule.BookParseRule


val novelSourceRepository by lazy { NovelSourceRepository() }
/**
 * 小说来源。后续可能还会存到数据库中
 */
class NovelSourceRepository {

    fun getAllBookParseRule(): Observable<List<BookParseRule>> {
        return Observable.fromCallable {
            NovelSourceConfig.getAllBookParseRule()
        }
    }

    fun saveBookParseRule(rule: BookParseRule): Observable<BookParseRule> {
        return Observable.fromCallable {
            NovelSourceConfig.saveBookParseRule(rule)
            rule
        }
    }

    fun deleteBookParseRule(rule: BookParseRule): Observable<BookParseRule> {
        return Observable.fromCallable {
            NovelSourceConfig.deleteBookParseRule(rule)
            rule
        }
    }


}
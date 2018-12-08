package sjj.fiction.data.repository

import io.reactivex.Observable
import sjj.fiction.data.source.remote.rule.BookParseRule


val fictionSourceRepository by lazy { FictionSourceRepository() }
/**
 * 小说来源。后续可能还会存到数据库中
 */
class FictionSourceRepository {

    fun getAllBookParseRule(): Observable<List<BookParseRule>> {
        return Observable.fromCallable {
            FictionSourceConfig.getAllBookParseRule()
        }
    }

    fun saveBookParseRule(rule: BookParseRule): Observable<BookParseRule> {
        return Observable.fromCallable {
            FictionSourceConfig.saveBookParseRule(rule)
            rule
        }
    }

    fun deleteBookParseRule(rule: BookParseRule): Observable<BookParseRule> {
        return Observable.fromCallable {
            FictionSourceConfig.deleteBookParseRule(rule)
            rule
        }
    }


}
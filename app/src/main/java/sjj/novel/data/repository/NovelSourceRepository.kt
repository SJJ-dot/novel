package sjj.novel.data.repository

import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import sjj.novel.data.source.local.booksDataBase
import sjj.novel.data.source.remote.DefaultNovelSource
import sjj.novel.data.source.remote.rule.BookParseRule


val novelSourceRepository by lazy { NovelSourceRepository() }
/**
 * 小说来源。后续可能还会存到数据库中
 */
class NovelSourceRepository {
    private val source by lazy { DefaultNovelSource() }
    fun getAllBookParseRule(): Flowable<List<BookParseRule>> {
        return booksDataBase.novelSourceDao().getAllBookParseRule().subscribeOn(Schedulers.io())
    }

    fun saveBookParseRule(rule: BookParseRule): Observable<BookParseRule> {
        return Observable.fromCallable {
            booksDataBase.novelSourceDao().saveBookParseRule(rule)
            rule
        }.subscribeOn(Schedulers.io())
    }

    fun deleteBookParseRule(rule: BookParseRule): Observable<BookParseRule> {
        return Observable.fromCallable {
            booksDataBase.novelSourceDao().deleteBookParseRule(rule)
            rule
        }.subscribeOn(Schedulers.io())
    }

    fun getDefaultNovelSourceRule(): Observable<List<BookParseRule>> {
        return source.getDefaultNovelSourceRule()
    }

    interface RemoteSource {
        fun getDefaultNovelSourceRule():Observable<List<BookParseRule>>
    }

}
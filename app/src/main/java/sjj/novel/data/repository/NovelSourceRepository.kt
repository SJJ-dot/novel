package sjj.novel.data.repository

import io.reactivex.Flowable
import io.reactivex.Observable
import sjj.novel.data.source.local.booksDataBase
import sjj.novel.data.source.remote.DefaultNovelSource
import sjj.novel.data.source.remote.rule.BookParseRule
import sjj.novel.util.fromCallableOrNull
import sjj.novel.util.subscribeOnSingle


val novelSourceRepository by lazy { NovelSourceRepository() }

/**
 * 小说来源。后续可能还会存到数据库中
 */
class NovelSourceRepository {
    private val source by lazy { DefaultNovelSource() }
    fun getAllBookParseRule(): Flowable<List<BookParseRule>> {
        return booksDataBase.novelSourceDao().getAllBookParseRule()
                .subscribeOnSingle()
    }

    fun getBookParseRule(tld: String): Observable<BookParseRule> {
        return Observable.fromCallable {
            booksDataBase.novelSourceDao().getBookParseRule(tld)
        }.subscribeOnSingle()
    }

    fun saveBookParseRule(rule: BookParseRule): Observable<BookParseRule> {
        return Observable.fromCallable {
            try {
                booksDataBase.novelSourceDao().insertBookParseRule(rule)
            } catch (e: Exception) {
                booksDataBase.novelSourceDao().updateBookParseRule(rule)
            }
            rule
        }.subscribeOnSingle()
    }

    fun deleteBookParseRule(rule: BookParseRule): Observable<BookParseRule> {
        return Observable.fromCallable {
            booksDataBase.novelSourceDao().deleteBookParseRule(rule)
            rule
        }.subscribeOnSingle()
    }

    fun getDefaultNovelSourceRule(): Observable<List<BookParseRule>> {
        return source.getDefaultNovelSourceRule()
                .subscribeOnSingle()
    }

    fun getBookParse(bookName: String, author: String): Observable<List<BookParseRule>> {
        return fromCallableOrNull {
            booksDataBase.novelSourceDao().getBookParseRule(bookName, author)
        }.subscribeOnSingle()
    }


}
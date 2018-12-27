package sjj.novel.view.module.source

import androidx.lifecycle.ViewModel
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.eclipse.egit.github.core.Issue
import sjj.novel.AppConfig
import sjj.novel.data.repository.novelSourceRepository
import sjj.novel.data.source.remote.githubDataSource
import sjj.novel.data.source.remote.rule.BookParseRule
import sjj.novel.util.gson

class NovelSourceViewModel : ViewModel() {
    fun getAllBookParseRule() = novelSourceRepository.getAllBookParseRule()

    fun saveBookParseRule(rule: BookParseRule): Observable<BookParseRule> {
        return novelSourceRepository.saveBookParseRule(rule)
    }

    fun deleteBookParseRule(rule: BookParseRule): Observable<BookParseRule> {
        return novelSourceRepository.deleteBookParseRule(rule)
    }

    fun syncNovelSource(): Observable<BookParseRule> {
        return novelSourceRepository.getDefaultNovelSourceRule().flatMap {
            AppConfig.defaultNovelSourceTLD = it.map { it.topLevelDomain }.toSet()
            Observable.fromIterable(it).flatMap(novelSourceRepository::saveBookParseRule)
        }
    }

    fun share(rule: BookParseRule): Observable<BookParseRule> {
        if (AppConfig.defaultNovelSourceTLD.contains(rule.topLevelDomain)) {
            return Observable.error(Exception("共享书源已存在"))
        }
        return Observable.just(rule).observeOn(Schedulers.io()).flatMap {
            githubDataSource.addIssue(Issue()
                    .setTitle("小说网站解析规则：${it.sourceName}")
                    .setBody(gson.toJson(it)))
                    .map {
                        rule
                    }
        }
    }

}
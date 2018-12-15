package sjj.novel.data.source.remote

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.eclipse.egit.github.core.Authorization
import org.eclipse.egit.github.core.Issue
import org.eclipse.egit.github.core.Label
import org.eclipse.egit.github.core.client.GitHubClient
import org.eclipse.egit.github.core.client.RequestException
import org.eclipse.egit.github.core.service.IssueService
import org.eclipse.egit.github.core.service.MarkdownService
import org.eclipse.egit.github.core.service.OAuthService
import sjj.alog.Log
import sjj.novel.AppConfig
import java.util.*

val githubDataSource by lazy { GithubDataSource() }

class GithubDataSource {
    private val USER_AGENT = "Novel/1.0"
    private val APP_NOTE = "Novel"
    private val SCOPES = listOf("repo", "user", "gist")

    fun addIssue(issue: Issue): Observable<Issue> {
        return Observable.fromCallable {
            try {
                val client = GitHubClient().setOAuth2Token(AppConfig.gitHubAuthToken.value)
                IssueService(client).createIssue("lTBeL", "novel", issue)
            } catch (e: RequestException) {
                throw Exception("未登录或授权已失效", e)
            }
        }.subscribeOn(Schedulers.io())
    }

    fun login(userName: String, password: String): Observable<String> {
        return Observable.fromCallable {
            val token = AppConfig.gitHubAuthToken.value ?: ""
            if (token.isNotEmpty()) {
                return@fromCallable token
            }
            val client = GitHubClient()
                    .setCredentials(userName, password)
                    .setUserAgent(USER_AGENT)
            val service = OAuthService(client)

            service.authorizations.forEach {
                if (it.scopes.containsAll(SCOPES) && it.token?.isNotBlank() == true) {
                    return@fromCallable it.token
                }
            }
            val auth = service.createAuthorization(Authorization()
                    .setNote("$APP_NOTE:${Date()}")
                    .setScopes(SCOPES))
            return@fromCallable auth?.token ?: throw Exception("登陆授权获取失败")
        }.subscribeOn(Schedulers.io()).doOnNext {
            //保存授权码
            AppConfig.gitHubAuthToken.value = it
        }

    }

    /**
     * 将markdown 文档渲染为html
     */
    fun getMarkDownHtml(text: String): Observable<String> {
        return Observable.fromCallable {
            MarkdownService().getHtml(text, "markdown")
        }.subscribeOn(Schedulers.io())
    }
}
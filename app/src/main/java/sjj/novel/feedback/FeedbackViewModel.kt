package sjj.novel.feedback

import android.arch.lifecycle.ViewModel
import android.databinding.ObservableField
import io.reactivex.Observable
import org.eclipse.egit.github.core.Issue
import sjj.novel.AppConfig
import sjj.novel.data.source.remote.githubDataSource
import java.lang.Exception

class FeedbackViewModel : ViewModel() {
    val issueTitle = ObservableField<String>()
    val issueDescription = ObservableField<String>()

    fun submit(): Observable<Issue> {
        val title = issueTitle.get()
        if (title.isNullOrEmpty()) {
            return Observable.error(Exception("标题不能为空"))
        }
        if (AppConfig.gitHubAuthToken.value?.isNotEmpty() != true) {
            return Observable.error(Exception("请先登陆"))
        }
        return githubDataSource.addIssue(Issue().setTitle(title).setBody(issueDescription.get()))
    }

}
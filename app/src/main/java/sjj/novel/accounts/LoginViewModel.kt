package sjj.novel.accounts

import android.arch.lifecycle.ViewModel
import android.databinding.ObservableField
import android.text.Html
import io.reactivex.Observable
import sjj.novel.R
import sjj.novel.Session
import sjj.novel.data.source.remote.githubDataSource
import java.lang.Exception

class LoginViewModel : ViewModel() {
    val loginPasswordExplanation =Html.fromHtml(Session.ctx.getString(R.string.login_pass_explanation))
    val userName = ObservableField<String>()
    val password = ObservableField<String>()
    fun login(): Observable<String> {
        val userName = userName.get()
        val password = password.get()
        if (userName.isNullOrEmpty()||password.isNullOrEmpty()) {
            return Observable.error<String>(Exception("用户名与密码不能为空"))
        }
       return githubDataSource.login(userName, password)
    }
}
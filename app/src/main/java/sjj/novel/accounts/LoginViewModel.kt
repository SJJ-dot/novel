package sjj.novel.accounts

import androidx.lifecycle.ViewModel
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import android.text.Html
import io.reactivex.Observable
import sjj.novel.AppConfig
import sjj.novel.R
import sjj.novel.Session
import sjj.novel.data.source.remote.githubDataSource
import java.lang.Exception

class LoginViewModel : ViewModel() {
    val userName = ObservableField<String>()
    val password = ObservableField<String>()

    fun login(): Observable<String> {
        val userName = userName.get()
        val password = password.get()

        if (password.isNullOrEmpty()) {
            return Observable.error(Exception("密码不能为空"))
        }

        if (userName.isNullOrEmpty()) {
            //授权令牌（Personal access token） 只填密码 视为token
            return Observable.fromCallable {
                AppConfig.gitHubAuthToken.value = password
                this.password.set("")
                password
            }
        }

        return githubDataSource.login(userName, password).doOnNext {
            this.userName.set("")
            this.password.set("")
        }
    }
}
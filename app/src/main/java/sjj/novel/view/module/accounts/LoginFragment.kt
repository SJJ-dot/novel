package sjj.novel.view.module.accounts


import androidx.databinding.DataBindingUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_login.*
import sjj.alog.Log
import sjj.novel.BaseFragment
import sjj.novel.R
import sjj.novel.databinding.FragmentLoginBinding
import sjj.novel.util.getModel
import sjj.novel.util.observeOnMain


class LoginFragment : BaseFragment() {
    private lateinit var model: LoginViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        model = getModel()
        val binding = DataBindingUtil.inflate<FragmentLoginBinding>(inflater, R.layout.fragment_login, container, false)
        binding.model = model
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        submit.setOnClickListener {
            showSnackbar(submit, "正在登陆……")
            model.login().observeOnMain().subscribe({
                showSnackbar(submit, "登陆成功")
                dismiss()
            }, { throwable ->
                showSnackbar(submit, "登陆失败:${throwable.message}")
                Log.e("登陆失败", throwable)
            })
        }
    }
}

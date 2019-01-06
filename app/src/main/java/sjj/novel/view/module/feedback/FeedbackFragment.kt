package sjj.novel.view.module.feedback


import androidx.lifecycle.Observer
import androidx.databinding.DataBindingUtil
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import android.view.*
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_feedback.*
import sjj.alog.Log
import sjj.novel.AppConfig
import sjj.novel.BaseFragment
import sjj.novel.R
import sjj.novel.view.module.accounts.LoginFragment
import sjj.novel.databinding.FragmentFeedbackBinding
import sjj.novel.util.getModel

/**
 *意见反馈
 */
class FeedbackFragment : BaseFragment() {

    private lateinit var model: FeedbackViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        model = getModel()

        val binding = DataBindingUtil.inflate<FragmentFeedbackBinding>(layoutInflater, R.layout.fragment_feedback, container, false)
        binding.model = model
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.e(this)
        submit.setOnClickListener { _ ->
            showSnackbar(submit, "正在提交……", Snackbar.LENGTH_INDEFINITE)
            model.submit().observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        showSnackbar(submit, "反馈成功")
                    }, {
                        showSnackbar(submit, "反馈失败:${it.message}", Snackbar.LENGTH_LONG)
                        Log.e("反馈失败", it)
                    })
                    .destroy("submit issue")
        }
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_login_menu, menu)
        AppConfig.gitHubAuthToken.observe(this, Observer {
            val item = menu.findItem(R.id.menu_login)
            item?.title = if (it.isNullOrBlank()) "登陆" else "已登录"
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_login -> {
                LoginFragment().show(fragmentManager!!)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}

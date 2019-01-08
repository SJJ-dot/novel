package sjj.novel.view.module.source


import androidx.databinding.DataBindingUtil
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_novel_source_rule_explanation.*
import sjj.novel.BaseActivity
import sjj.novel.R
import sjj.novel.databinding.ActivityNovelSourceRuleExplanationBinding
import sjj.novel.util.observeOnMain

/**
 *小说源编辑规则说明
 */
class NovelSourceRuleExplanationActivity : BaseActivity() {
    private val model by lazy { NovelSourceRuleExplanationViewModel() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bind = DataBindingUtil.setContentView<ActivityNovelSourceRuleExplanationBinding>(this, R.layout.activity_novel_source_rule_explanation)
        bind.model = model
        val settings = text.settings
        settings.builtInZoomControls = true
        settings.displayZoomControls = false
        showSnackbar(text,"加载中……", Snackbar.LENGTH_INDEFINITE)
        model.refresh().observeOnMain().subscribe({
            showSnackbar(text,"加载成功")
            text.loadData(it, "text/html", "utf8")
        }, {
            showSnackbar(text, "加载失败:${it.message}")
        }).destroy("load _novel_source_rule_explanation")
    }


}

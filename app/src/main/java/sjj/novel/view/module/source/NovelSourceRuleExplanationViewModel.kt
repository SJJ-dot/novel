package sjj.novel.view.module.source

import androidx.lifecycle.ViewModel
import androidx.databinding.ObservableField
import android.text.Html
import android.text.Spanned
import io.reactivex.Observable
import sjj.novel.data.source.remote.defaultNovelSource

class NovelSourceRuleExplanationViewModel : ViewModel() {
    val ruleExplanation = ObservableField<Spanned>()
    fun refresh(): Observable<String> {
        return defaultNovelSource.getNovelSourceRuleExplanation().doOnNext {
            ruleExplanation.set(Html.fromHtml(it))
        }
    }
}
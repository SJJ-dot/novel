package sjj.novel.source

import android.arch.lifecycle.ViewModel
import android.databinding.ObservableField
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
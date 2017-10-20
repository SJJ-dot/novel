package sjj.fiction.main.impl

import android.support.v4.app.FragmentManager
import android.view.View
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.app_bar_main.*
import sjj.fiction.R
import sjj.fiction.data.Repository.FictionDataRepository
import sjj.fiction.main.MainContract
import sjj.fiction.search.SearchFragment
import sjj.fiction.util.fictionDataRepository
import sjj.fiction.util.showSoftInput

/**
 * Created by SJJ on 2017/10/7.
 */
class MainPresenter(private val view: MainContract.View) : MainContract.Presenter {
    val fiction: FictionDataRepository = fictionDataRepository

    init {
        view.setPresenter(this)
    }

    override fun start() {
    }

    override fun stop() {
    }

    override fun showAutoText() {
        fiction.getSearchHistory()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    view.showAutoText(it)
                }, {})
    }

}
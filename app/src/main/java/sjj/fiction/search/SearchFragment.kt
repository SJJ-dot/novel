package sjj.fiction.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import sjj.alog.Log
import sjj.fiction.BaseFragment
import sjj.fiction.R
import sjj.fiction.data.Repository.SoduDataRepository
import sjj.fiction.util.DATA_REPOSITORY_SODU
import sjj.fiction.util.DataRepository

/**
 * Created by SJJ on 2017/10/7.
 */
class SearchFragment : BaseFragment() {
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_search, container, false)
    }

    fun search(text: String) {
        Log.e(text)
        if (text.isEmpty()) return
        val data: SoduDataRepository = DataRepository[DATA_REPOSITORY_SODU]
        data.search(text).subscribe({Log.e(it)},{Log.e("error",it)})
    }
}
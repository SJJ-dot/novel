package sjj.fiction.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import sjj.fiction.BaseFragment
import sjj.fiction.R

class AboutFragment : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_about, container, false)
    }
}

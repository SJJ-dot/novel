package sjj.fiction.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tencent.bugly.beta.Beta
import kotlinx.android.synthetic.main.fragment_about.*
import sjj.fiction.BaseFragment
import sjj.fiction.BuildConfig
import sjj.fiction.R

class AboutFragment : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_about, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        versionCode.text = "版本号：${BuildConfig.VERSION_NAME}"
        versionCode.setOnClickListener {
            Beta.checkUpgrade(true,false)
        }
    }
}

package sjj.novel.view.module.read


import androidx.lifecycle.Observer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_reader_font_setting.*
import sjj.novel.AppConfig
import sjj.novel.BaseFragment
import sjj.novel.R


/**
 *字体设置
 */
class ReaderFontSettingFragment : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_reader_font_setting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        font_minus.setOnClickListener {
            AppConfig.fontSize.value = AppConfig.fontSize.value!! - 1
        }
        font_add.setOnClickListener {
            AppConfig.fontSize.value = AppConfig.fontSize.value!! + 1
        }

        AppConfig.fontSize.observe(this, Observer {
            font_current.text = it.toString()
        })
    }


}

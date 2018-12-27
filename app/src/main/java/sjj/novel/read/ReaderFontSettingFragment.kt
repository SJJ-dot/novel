package sjj.novel.read


import androidx.lifecycle.Observer
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import kotlinx.android.synthetic.main.fragment_reader_font_setting.*
import kotlinx.android.synthetic.main.fragment_reader_light_setting.*
import kotlinx.android.synthetic.main.item_page_style.view.*
import org.jetbrains.anko.sdk27.coroutines.onSeekBarChangeListener
import sjj.novel.AppConfig
import sjj.novel.BaseFragment
import sjj.novel.R
import sjj.novel.util.id
import sjj.novel.util.setScreenBrightness
import sjj.novel.view.BaseAdapter
import sjj.novel.view.reader.page.PageStyle


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

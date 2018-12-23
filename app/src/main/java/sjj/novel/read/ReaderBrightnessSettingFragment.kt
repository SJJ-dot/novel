package sjj.novel.read


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import kotlinx.android.synthetic.main.fragment_reader_light_setting.*
import org.jetbrains.anko.sdk27.coroutines.onSeekBarChangeListener
import sjj.novel.AppConfig
import sjj.novel.BaseFragment
import sjj.novel.R
import sjj.novel.util.getScreenBrightness
import sjj.novel.util.setScreenBrightness


/**
 * 亮度 颜色风格设置
 */
class ReaderBrightnessSettingFragment : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_reader_light_setting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        light_follow_sys.isChecked = AppConfig.isBrightnessFollowSys
        brightness.isEnabled = !light_follow_sys.isChecked
        light_follow_sys.setOnCheckedChangeListener { _, isChecked ->
            AppConfig.isBrightnessFollowSys = isChecked
            brightness.isEnabled = !isChecked
            if (isChecked) {
                setScreenBrightness(activity!!, WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE)
            } else {
                setScreenBrightness(activity!!, AppConfig.screenBrightnessProgress)
            }
        }

        brightness.progress = (AppConfig.screenBrightnessProgress * brightness.max).toInt()
        brightness.onSeekBarChangeListener {
            onProgressChanged { seekBar, i, b ->
                val fl = i.toFloat() / seekBar!!.max
                AppConfig.screenBrightnessProgress = fl
                setScreenBrightness(activity!!, fl)
            }
        }


    }


}

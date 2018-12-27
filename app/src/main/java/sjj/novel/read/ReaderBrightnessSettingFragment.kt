package sjj.novel.read


import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
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
        val data = PageStyle.values().map {
            it.getBackgroundDrawable(context) to it
        }
        recycle_view.layoutManager = androidx.recyclerview.widget.GridLayoutManager(context, data.size)
        recycle_view.adapter = Adapter(data)
    }

    class Adapter(val data:List<Pair<Drawable,PageStyle>>) : BaseAdapter() {

        override fun itemLayoutRes(viewType: Int): Int = R.layout.item_page_style

        override fun getItemCount(): Int = data.size

        override fun getItemId(position: Int): Long {
            return data[position].second.ordinal.toLong()
        }

        override fun onBindViewHolder(p0: androidx.recyclerview.widget.RecyclerView.ViewHolder, p1: Int) {
            p0.itemView.page_style.background = data[p1].first
            p0.itemView.selected_flag.isChecked = AppConfig.readerPageStyle.value == data[p1].second
            p0.itemView.selected_flag.visibility = if (p0.itemView.selected_flag.isChecked) View.VISIBLE else View.INVISIBLE
            p0.itemView.setOnClickListener {
                AppConfig.readerPageStyle.value = data[p1].second
                notifyDataSetChanged()
            }
        }

    }



}

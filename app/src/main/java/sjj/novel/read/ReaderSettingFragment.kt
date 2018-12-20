package sjj.novel.read


import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.SeekBar
import kotlinx.android.synthetic.main.fragment_reader_setting.*
import org.jetbrains.anko.support.v4.toast
import sjj.alog.Log
import sjj.novel.BaseFragment
import sjj.novel.R
import sjj.novel.view.reader.page.PageLoader

/**
 *阅读器设置菜单
 *
 */
class ReaderSettingFragment : BaseFragment() {

    var callBack: CallBack? = null
        get() {
            return when {
                field != null -> field
                context is CallBack -> context as CallBack
                else -> null
            }
        }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_reader_setting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        read_tv_pre_chapter.setOnClickListener {
            callBack?.getPageLoader()?.skipPreChapter()
        }
        read_tv_next_chapter.setOnClickListener {
            callBack?.getPageLoader()?.skipNextChapter()
        }

        read_sb_chapter_progress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (isVisible) {
                    read_tv_page_tip.text = "${progress + 1}/${read_sb_chapter_progress.max + 1}"
                    read_tv_page_tip.visibility = VISIBLE
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                //进行切换
                val pagePos = read_sb_chapter_progress.progress
                val pagePos1 = callBack?.getPageLoader()?.pagePos
                if (pagePos1 != null && pagePos != pagePos1) {
                    callBack?.getPageLoader()?.skipToPage(pagePos)
                }
                //隐藏提示
                read_tv_page_tip.visibility = INVISIBLE
            }
        })
        read_tv_category.setOnClickListener {
            callBack?.openChapterList()
        }
        read_tv_night_mode.setOnClickListener {
            toast("nonsupport")
        }
        read_tv_setting.setOnClickListener {
            toast("nonsupport")
        }

    }

    override fun onStart() {
        super.onStart()
        callBack?.getPageLoader()?.also {
            setPageCount(it.pageCount)
            setPagePos(it.pagePos)
        }
    }

    /**
     * 设置当前章节页数
     */
    fun setPageCount(count: Int) {
        read_sb_chapter_progress?.max = maxOf(0, count - 1)
        read_sb_chapter_progress?.progress = 0
        val loader = callBack?.getPageLoader()
        read_sb_chapter_progress?.isEnabled = !(loader == null || loader.pageStatus == PageLoader.STATUS_LOADING || loader.pageStatus == PageLoader.STATUS_ERROR)
    }

    /**
     * 当前页数
     */
    fun setPagePos(pos: Int) {
        read_sb_chapter_progress?.progress = pos
    }

    interface CallBack {
        /**
         * 开启章节目录
         */
        fun openChapterList()

        fun getPageLoader(): PageLoader?


    }


}

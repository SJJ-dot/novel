package sjj.novel.view.module.read


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import androidx.navigation.fragment.NavHostFragment
import kotlinx.android.synthetic.main.fragment_download_chapter.*
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.selector
import sjj.alog.Log
import sjj.novel.BaseFragment
import sjj.novel.R
import sjj.novel.databinding.FragmentDownloadChapterBinding
import sjj.novel.util.getModel
import sjj.novel.util.getModelActivity
import sjj.novel.util.observeOnMain
import sjj.rx.destroy
import java.lang.IllegalArgumentException

/**
 *
 */
class DownloadChapterFragment : BaseFragment() {

    companion object {
        const val BOOK_NAME = "BOOK_NAME"
        const val BOOK_AUTHOR = "BOOK_AUTHOR"
        fun create(bookName: String, bookAuthor: String): DownloadChapterFragment {
            val args = Bundle()
            args.putString(BOOK_NAME, bookName)
            args.putString(BOOK_AUTHOR, bookAuthor)
            val fragment = DownloadChapterFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var model: DownChapterViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_download_chapter, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        model = getModelActivity() {
            arguments?.let {
                arrayOf(arguments!!.get(BOOK_NAME), arguments!!.get(BOOK_AUTHOR))
            } ?: throw IllegalArgumentException()
        }
        val bind = DataBindingUtil.bind<FragmentDownloadChapterBinding>(view)
        bind!!.setLifecycleOwner(this)
        bind.model = model
        cancel.setOnClickListener {
            NavHostFragment.findNavController(this).navigateUp()
        }

        download_start_chapter.setOnClickListener {
            val list = model.chapterList.value ?: return@setOnClickListener
            val record = model.startChapter.value ?: return@setOnClickListener
            AlertDialog.Builder(context!!).setTitle("选择章节下载起始位置：")
                    .setSingleChoiceItems(list.map { it.chapterName }.toTypedArray(), record.index) { dialog, which ->
                        model.startChapter.setValue(list[which])
                        dialog.dismiss()
                    }.show()
        }
        download_end_chapter.setOnClickListener {
            val list = model.chapterList.value ?: return@setOnClickListener
            val chapter = model.endChapter.value ?: return@setOnClickListener
            AlertDialog.Builder(context!!).setTitle("选择章节下载起始位置：")
                    .setSingleChoiceItems(list.map { it.chapterName }.toTypedArray(), chapter.index) { dialog, which ->
                        model.endChapter.setValue(list[which])
                        dialog.dismiss()
                    }.show()
        }
        download.setOnClickListener {
            showSnackbar(download, "章节缓存中……")
            model.cachedBookChapter()
                    .observeOnMain()
                    .doOnError {
                        showSnackbar(download, "章节缓存失败：${it.message}")
                    }.doOnComplete {
                        showSnackbar(download, "章节缓存完成")
                    }
                    .subscribe().destroy("fragment download chapter cache chapter", activity!!.lifecycle)

        }
    }


}

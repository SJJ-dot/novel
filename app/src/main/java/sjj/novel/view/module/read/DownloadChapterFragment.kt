package sjj.novel.view.module.read


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import sjj.novel.R
import sjj.novel.databinding.FragmentDownloadChapterBinding
import sjj.novel.util.getModel
import sjj.novel.util.getModelActivity
import java.lang.IllegalArgumentException

/**
 *
 */
class DownloadChapterFragment : Fragment() {

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
        bind!!.model = model
    }


}

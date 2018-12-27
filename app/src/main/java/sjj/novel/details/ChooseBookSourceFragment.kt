package sjj.novel.details


import androidx.databinding.DataBindingUtil
import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_choose_book_source.*
import sjj.novel.BaseFragment
import sjj.novel.R
import sjj.novel.databinding.FragmentChooseBookSourceBinding
import sjj.novel.util.getModel


/**
 *书籍换源 刷新
 */
class ChooseBookSourceFragment : BaseFragment() {

    companion object {
        const val BOOK_NAME = "BOOK_NAME"
        const val BOOK_AUTHOR = "BOOK_AUTHOR"

        fun newInstance(name: String, author: String): ChooseBookSourceFragment {
            return ChooseBookSourceFragment().apply {
                arguments = Bundle().apply {
                    putString(BOOK_NAME, name)
                    putString(BOOK_AUTHOR, author)
                }
            }
        }

    }

    private lateinit var model: ChooseBookSourceViewModel
    private val adapter = ChooseBookSourceAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        model = getModel {
            arrayOf(arguments!!.getString(sjj.novel.details.ChooseBookSourceFragment.Companion.BOOK_NAME)!!, arguments!!.getString(sjj.novel.details.ChooseBookSourceFragment.Companion.BOOK_AUTHOR)!!)
        }

        val binding = DataBindingUtil.inflate<FragmentChooseBookSourceBinding>(inflater, R.layout.fragment_choose_book_source, container, false)
        binding.model = model
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter.setHasStableIds(true)
        books.adapter = adapter
        model.fillViewModel().observeOn(AndroidSchedulers.mainThread()).subscribe {
            adapter.data = it
            adapter.notifyDataSetChanged()
        }.destroy("ChooseBookSourceFragment fill view model")
        swipe.setOnRefreshListener {
            swipe.isRefreshing = false
            model.refresh().subscribe().destroy("ChooseBookSourceFragment refresh book")
        }
    }

    private inner class ChooseBookSourceAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {
        var data = listOf<ChooseBookSourceViewModel.ChooseBookSourceItemViewModel>()
        override fun getItemCount(): Int = data.size
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
            val binding = DataBindingUtil.inflate<sjj.novel.databinding.ItemDetailsBookSourceListBinding>(LayoutInflater.from(parent.context), R.layout.item_details_book_source_list, parent, false)
            return object : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {}
        }

        override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
            val bind = DataBindingUtil.bind<sjj.novel.databinding.ItemDetailsBookSourceListBinding>(holder.itemView)
            val bookGroup = data[position]
            bind?.model = bookGroup
            holder.itemView.setOnClickListener { _ ->
                model.setBookSource(bookGroup.book).observeOn(AndroidSchedulers.mainThread()).subscribe {
                    dismiss()
                }.destroy("set novel source")
            }
        }

        override fun getItemId(position: Int): Long {
            return data[position].id
        }

    }

}

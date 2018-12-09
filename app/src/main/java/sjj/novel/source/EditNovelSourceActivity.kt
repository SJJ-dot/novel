package sjj.novel.source

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import kotlinx.android.synthetic.main.activity_edit_novel_source.*
import sjj.alog.Log
import sjj.novel.BaseActivity
import sjj.novel.R
import sjj.novel.databinding.ActivityEditNovelSourceBinding
import sjj.novel.databinding.NovelSourceSearchResultRuleBinding
import sjj.novel.util.getModel

class EditNovelSourceActivity : BaseActivity() {
    companion object {
        const val NOVEL_SOURCE_TOP_LEVEL_DOMAIN = "NOVEL_SOURCE_TOP_LEVEL_DOMAIN"
    }

    private val model by lazy {
        getModel<EditNovelSourceViewModel>(object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return modelClass.getConstructor(String::class.java).newInstance(intent.getStringExtra(NOVEL_SOURCE_TOP_LEVEL_DOMAIN))
            }
        })
    }

    private val adapter by lazy { SearchResultPagerAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityEditNovelSourceBinding>(this, R.layout.activity_edit_novel_source)
//        setContentView(R.layout.activity_edit_novel_source)

        setSupportActionBar(toolbar)
        val supportActionBar = supportActionBar!!
        supportActionBar.setDisplayHomeAsUpEnabled(true)

        binding.model = model

        search_rule_result.adapter = adapter
        search_rule_result.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        initData()
    }

    private fun initData() {
        model.fillViewModel().subscribe {
            adapter.data = model.searchResultViewModel
            adapter.notifyDataSetChanged()

            add_search_result_rule.setOnClickListener {
                adapter.data?.add(EditNovelSourceViewModel.SearchResultViewModel())
                adapter.notifyDataSetChanged()
            }

        }.destroy("menu_reset_novel_source")
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_read_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.menu_reset_novel_source -> {
                initData()
                true
            }
            R.id.menu_save_novel_source -> {
                model.saveNovelSourceParseRule()
                        .subscribe()
                        .destroy("menu_save_novel_source")
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    inner class SearchResultPagerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var data: MutableList<EditNovelSourceViewModel.SearchResultViewModel>? = null
        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
            val inflate = DataBindingUtil.inflate<NovelSourceSearchResultRuleBinding>(layoutInflater, R.layout.novel_source_search_result_rule, p0, false)
            return object : RecyclerView.ViewHolder(inflate.root) {
            }
        }

        override fun getItemCount(): Int = data?.size ?: 0

        override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {
            val bind = DataBindingUtil.bind<NovelSourceSearchResultRuleBinding>(p0.itemView)
            bind?.model = data!![p1]
            bind?.delete?.setOnClickListener {
                data!!.removeAt(p1)
                notifyDataSetChanged()
            }
        }
    }

}

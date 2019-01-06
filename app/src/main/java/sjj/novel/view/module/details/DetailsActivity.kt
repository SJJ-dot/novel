package sjj.novel.view.module.details

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_details.*
import org.jetbrains.anko.startActivity
import sjj.novel.BaseActivity
import sjj.novel.R
import sjj.novel.databinding.ActivityDetailsBinding
import sjj.novel.model.Chapter
import sjj.novel.util.getModel
import sjj.novel.util.observeOnMain
import sjj.novel.util.requestOptions
import sjj.novel.view.fragment.ChapterListFragment
import sjj.novel.view.fragment.ChooseBookSourceFragment
import sjj.novel.view.module.read.ReadActivity

/**
 * Created by SJJ on 2017/10/10.
 */
class DetailsActivity : BaseActivity(),ChapterListFragment.ItemClickListener {
    companion object {
        const val BOOK_NAME = "book_name"
        const val BOOK_AUTHOR = "book_author"
    }

    private lateinit var model: DetailsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        model = getModel { arrayOf(intent.getStringExtra(BOOK_NAME), intent.getStringExtra(BOOK_AUTHOR)) }

        supportFragmentManager.beginTransaction()
                .replace(R.id.chapter_list,ChapterListFragment.create(model.name,model.author))
                .commitAllowingStateLoss()

        val bind: ActivityDetailsBinding = DataBindingUtil.setContentView(this, R.layout.activity_details)

        model.bookCoverImgUrl.onBackpressureLatest().observeOnMain().subscribe {
            Glide.with(this)
                    .applyDefaultRequestOptions(requestOptions)
                    .load(it)
                    .into(bookCover)
        }.destroy()

        bind.model = model
        originWebsite.setOnClickListener { _ ->
            ChooseBookSourceFragment.newInstance(model.name, model.author).show(supportFragmentManager)
        }
        model.fillViewModel().observeOnMain().subscribe {book->
            detailsRefreshLayout.setOnRefreshListener {
                model.refresh(book).subscribe().destroy("details activity refresh book")
            }

            reading.setOnClickListener { _ ->
                model.bookSourceRecord.firstElement().observeOnMain().subscribe {
                    startActivity<ReadActivity>(ReadActivity.BOOK_NAME to model.name, ReadActivity.BOOK_AUTHOR to model.author)
                }.destroy("load book source record")
            }

            val lastChapter = book.lastChapter
            if (lastChapter != null) {
                latestChapter.setOnClickListener { v ->
                    v.isEnabled = false
                    model.setReadIndex(lastChapter).observeOnMain().doOnTerminate {
                        v.isEnabled = true
                    }.subscribe {
                        startActivity<ReadActivity>(ReadActivity.BOOK_NAME to model.name, ReadActivity.BOOK_AUTHOR to model.author)
                    }
                }
            } else {
                latestChapter.text = "无章节信息"
                latestChapter.isClickable = false
            }

        }.destroy("book details activity")
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_details_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.chapter_list -> {
                drawer_layout.openDrawer(GravityCompat.END)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.END)) {
            drawer_layout.closeDrawer(GravityCompat.END)
        } else {
            super.onBackPressed()
        }
    }

    override fun onClick(chapter: Chapter) {
        model.setReadIndex(chapter).observeOnMain().subscribe {
            startActivity<ReadActivity>(ReadActivity.BOOK_NAME to model.name, ReadActivity.BOOK_AUTHOR to model.author)
        }.destroy("details activity start read novel")
    }
}
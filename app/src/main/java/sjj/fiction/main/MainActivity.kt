package sjj.fiction.main

import android.app.ProgressDialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.toast
import sjj.alog.Log
import sjj.fiction.AppConfig
import sjj.fiction.BaseActivity
import sjj.fiction.R
import sjj.fiction.about.AboutActivity
import sjj.fiction.account.AccountActivity
import sjj.fiction.books.BookrackFragment
import sjj.fiction.main.impl.MainPresenter
import sjj.fiction.model.Book
import sjj.fiction.model.BookGroup
import sjj.fiction.search.SearchFragment
import sjj.fiction.util.getFragment
import sjj.fiction.util.hideSoftInput
import sjj.fiction.util.isDoubleClick
import sjj.fiction.util.showSoftInput


class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener, MainContract.View {
    private val tag_books = "tag_books"
    private val tag_search = "tag_search"
    private lateinit var presenter: MainContract.Presenter
    private var searchDialog: ProgressDialog? = null
    private val toggle by lazy { ActionBarDrawerToggle(this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)
        showBooksFragment(true)
        searchText.setOnClickListener {
            showBooksFragment(false)
        }
        searchCancel.setOnClickListener {
            if (searchInput.visibility == View.GONE) return@setOnClickListener
            if (searchInput.text.isNotEmpty()) {
                searchInput.setText("")
            } else {
                showBooksFragment(true)
            }
        }
        searchInput.setAdapter(ArrayAdapter<String>(this, R.layout.item_text_text, R.id.text1))
        searchInput.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                presenter.search(searchInput.text.toString())
                return@OnEditorActionListener true
            }
            false
        })
        searchInput.setOnClickListener {
            val arrayAdapter = searchInput.adapter as? ArrayAdapter<String>
                    ?: return@setOnClickListener
            arrayAdapter.clear()
            Log.e(AppConfig.searchHistory)
            arrayAdapter.addAll(AppConfig.searchHistory)
            searchInput.showDropDown()
        }
        MainPresenter(this)
        val model = ViewModelProviders.of(this).get(MainViewModel::class.java)
        model.books.observe(this, Observer {

        })
    }

    override fun onStart() {
        super.onStart()
        presenter.start()
    }

    override fun onStop() {
        super.onStop()
        presenter.stop()
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else if (!getFragment<SearchFragment>(tag_search).isHidden) {
            showBooksFragment(true)
        } else {
            if (isDoubleClick(nav_view))
                super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_about -> {
                startActivity(Intent(this, AboutActivity::class.java))
            }
            R.id.nav_account -> startActivity(Intent(this, AccountActivity::class.java))
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun setPresenter(presenter: MainContract.Presenter) {
        this.presenter = presenter
    }

    override fun setSearchBookList(book: List<BookGroup>) {
        getFragment<SearchFragment>(tag_search).showBookList(book)
    }

    override fun setSearchErrorHint(throwable: Throwable) {
        toast("搜索出错：${throwable.message}")
    }

    override fun setSearchProgressHint(active: Boolean) {
        if (active) {
            val searchDialog = searchDialog ?: indeterminateProgressDialog("请稍候……")
            if (!searchDialog.isShowing)
                searchDialog.show()
            this.searchDialog = searchDialog
        } else {
            searchDialog?.dismiss()
            searchDialog == null
        }
    }

    private fun showBooksFragment(show: Boolean) {

        if (show) {
            hideSoftInput(searchInput)
            searchInput.visibility = View.GONE
            searchText.visibility = View.VISIBLE
            toggle.isDrawerIndicatorEnabled = true
        } else {
            searchText.visibility = View.GONE
            searchInput.visibility = View.VISIBLE
            searchInput.requestFocus()
            showSoftInput(searchInput)
            toggle.isDrawerIndicatorEnabled = false
            searchInput.showDropDown()
        }


        val books = getFragment<BookrackFragment>(tag_books)
        val search = getFragment<SearchFragment>(tag_search)
        supportFragmentManager.beginTransaction()
                .hide(if (show) search else books)
                .show(if (show) books else search)
                .commit()
    }

    private inline fun <reified T : Fragment> getFragment(tag: String): T = getFragment<T>(R.id.contentMain, tag)
}

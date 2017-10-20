package sjj.fiction.main

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
import sjj.alog.Log
import sjj.fiction.BaseActivity
import sjj.fiction.R
import sjj.fiction.about.AboutActivity
import sjj.fiction.books.BookrackFragment
import sjj.fiction.search.SearchFragment
import sjj.fiction.util.getFragment
import sjj.fiction.util.hideSoftInput
import sjj.fiction.util.showSoftInput


class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener, SearchFragment.AutoTextChangeCallback {
    private val tag_books = "tag_books"
    private val tag_search = "tag_search"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        val toggle = ActionBarDrawerToggle(this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)
        showBooks(true)
        searchText.setOnClickListener {
            it.visibility = View.GONE
            searchInput.visibility = View.VISIBLE
            searchInput.requestFocus()
            showSoftInput(searchInput)
            toggle.isDrawerIndicatorEnabled = false
            showBooks(false)
            searchInput.showDropDown()
        }
        searchCancel.setOnClickListener {
            if (searchInput.visibility == View.GONE) return@setOnClickListener
            if (searchInput.text.isNotEmpty()) {
                searchInput.setText("")
            } else {
                hideSoftInput(searchInput)
                searchInput.visibility = View.GONE
                searchText.visibility = View.VISIBLE
                toggle.isDrawerIndicatorEnabled = true
                showBooks(true)
            }
        }
        searchInput.setAdapter(ArrayAdapter<String>(this, R.layout.item_text_text, R.id.text1))
        searchInput.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                getFragment<SearchFragment>(tag_search).search(searchInput.text.toString())
                return@OnEditorActionListener true
            }
            false
        })
        searchInput.setOnClickListener { searchInput.showDropDown() }
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_about -> {
                startActivity(Intent(this, AboutActivity::class.java))
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun notifyAutoTextChange(texts: List<String>) {
        val arrayAdapter = searchInput.adapter as? ArrayAdapter<String> ?: return
        arrayAdapter.clear()
        arrayAdapter.addAll(texts)
    }

    private fun showBooks(show: Boolean) {
        val books = getFragment<BookrackFragment>(tag_books)
        val search = getFragment<SearchFragment>(tag_search)
        supportFragmentManager.beginTransaction()
                .hide(if (show) search else books)
                .show(if (show) books else search)
                .commit()
    }

    private inline fun <reified T : Fragment> getFragment(tag: String): T = getFragment<T>(R.id.contentMain, tag)
}

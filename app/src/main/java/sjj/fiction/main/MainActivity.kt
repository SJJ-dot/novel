package sjj.fiction.main

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import sjj.fiction.R
import sjj.fiction.about.AboutActivity
import sjj.fiction.books.BookrackFragment
import sjj.fiction.search.SearchFragment
import sjj.fiction.util.hideSoftInput
import sjj.fiction.util.showSoftInput


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
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

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().add(R.id.contentMain, BookrackFragment(), tag_books).commit()
        } else {
            supportFragmentManager.beginTransaction()
                    .hide(supportFragmentManager.findFragmentByTag(tag_search))
                    .show(supportFragmentManager.findFragmentByTag(tag_books))
                    .commit()
        }

        searchText.setOnClickListener {
            it.visibility = View.GONE
            searchInput.visibility = View.VISIBLE
            searchInput.requestFocus()
            showSoftInput(searchInput)
            toggle.isDrawerIndicatorEnabled = false
            val b = supportFragmentManager.beginTransaction()
            b.hide(supportFragmentManager.findFragmentByTag(tag_books))
            if (supportFragmentManager.findFragmentByTag(tag_search) == null) {
                b.add(R.id.contentMain, SearchFragment(), tag_search)
            } else {
                b.show(supportFragmentManager.findFragmentByTag(tag_search))
            }
            b.commit()
        }
        searchCancel.setOnClickListener {
            if (searchInput.visibility == View.GONE) return@setOnClickListener
            hideSoftInput(searchInput)
            searchInput.visibility = View.GONE
            searchText.visibility = View.VISIBLE
            toggle.isDrawerIndicatorEnabled = true
            supportFragmentManager.beginTransaction()
                    .hide(supportFragmentManager.findFragmentByTag(tag_search))
                    .show(supportFragmentManager.findFragmentByTag(tag_books))
                    .commit()
        }
        searchInput.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val byTag = supportFragmentManager.findFragmentByTag(tag_search) as? SearchFragment
                if (byTag != null) {
                    byTag.search(searchInput.text.toString())
                }
                return@OnEditorActionListener true
            }
            false
        })
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
}

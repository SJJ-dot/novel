package sjj.fiction.main

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import kotlinx.android.synthetic.main.activity_main.*
import sjj.fiction.BaseActivity
import sjj.fiction.R


class MainActivity : BaseActivity() {
    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        NavigationUI.setupWithNavController(nav_ui, Navigation.findNavController(this, R.id.nav_host_fragment_main))
    }
}

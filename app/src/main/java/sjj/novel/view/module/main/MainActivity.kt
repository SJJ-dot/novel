package sjj.novel.view.module.main

import android.Manifest
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.longToast
import sjj.alog.Log
import sjj.novel.AppConfig
import sjj.novel.BaseActivity
import sjj.novel.R
import sjj.permission.PermissionCallback
import sjj.permission.model.Permission
import sjj.permission.util.PermissionUtil


class MainActivity : BaseActivity() {

    //lazy 有bug 需要绑定activity 生命周期
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration:AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        navController = Navigation.findNavController(this, R.id.nav_host_fragment_main)
        appBarConfiguration = AppBarConfiguration.Builder(navController.graph)
                .setDrawerLayout(drawer_layout)
                .build()

        setupActionBarWithNavController(this, navController, appBarConfiguration)
        NavigationUI.setupWithNavController(nav_ui, navController)

        val pers = arrayOf(Manifest.permission.INTERNET,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.READ_PHONE_STATE)
        PermissionUtil.requestPermissions(this, pers, object : PermissionCallback {
            override fun onGranted(permissions: Permission) {
                val set = AppConfig.deniedPermissions
                set.remove(permissions.name)
                AppConfig.deniedPermissions = set
            }

            override fun onDenied(permissions: Permission) {
                val s = "权限申请被拒绝：$permissions"
                Log.i(s)

                val set = AppConfig.deniedPermissions
                if (set.contains(permissions.name))
                    return
                set.add(permissions.name)
                AppConfig.deniedPermissions = set
                //如果权限被拒绝提醒一次
                longToast(s)
            }
        })

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                false
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}

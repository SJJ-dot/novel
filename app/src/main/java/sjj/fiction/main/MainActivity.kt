package sjj.fiction.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import io.reactivex.*
import io.reactivex.processors.PublishProcessor
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.longToast
import sjj.alog.Log
import sjj.fiction.AppConfig
import sjj.fiction.BaseActivity
import sjj.fiction.R
import sjj.permission.PermissionCallback
import sjj.permission.model.Permission
import sjj.permission.util.PermissionUtil


class MainActivity : BaseActivity() {
    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        NavigationUI.setupWithNavController(nav_ui, Navigation.findNavController(this, R.id.nav_host_fragment_main))

        val pers = arrayOf(Manifest.permission.INTERNET,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.READ_PHONE_STATE)
        PermissionUtil.requestPermissions(this,pers, object : PermissionCallback {
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
}

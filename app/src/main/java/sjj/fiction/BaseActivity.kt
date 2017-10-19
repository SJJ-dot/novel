package sjj.fiction

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import sjj.permission.util.PermissionUtil
import android.content.pm.PackageManager
import android.content.pm.PackageInfo
import org.jetbrains.anko.toast
import sjj.alog.Log
import sjj.permission.PermissionCallback
import sjj.permission.model.Permission


/**
 * Created by SJJ on 2017/10/5.
 */
abstract class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.app.activitys.add(this)
        val packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
        val requestedPermissions = packageInfo.requestedPermissions
        PermissionUtil.requestPermissions(this, requestedPermissions, object : PermissionCallback {

            override fun onGranted(permissions: Permission?) {
            }

            override fun onDenied(permissions: Permission?) {
                val s = "权限申请被拒绝：${permissions.toString()}"
                toast(s)
                Log.e(s)
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        App.app.activitys.remove(this)
    }
}
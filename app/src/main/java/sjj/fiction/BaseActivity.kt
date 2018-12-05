package sjj.fiction

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import io.reactivex.disposables.Disposable
import org.jetbrains.anko.longToast
import sjj.alog.Log
import sjj.fiction.util.destroy
import sjj.fiction.util.pause
import sjj.fiction.util.stop
import sjj.permission.PermissionCallback
import sjj.permission.model.Permission
import sjj.permission.util.PermissionUtil


/**
 * Created by SJJ on 2017/10/5.
 */
abstract class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Session.activitys.add(this)
        val packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
        val requestedPermissions = packageInfo.requestedPermissions

        PermissionUtil.requestPermissions(this, requestedPermissions, object : PermissionCallback {

            override fun onGranted(permissions: Permission) {
                val set = AppConfig.deniedPermissions
                set.remove(permissions.name)
                AppConfig.deniedPermissions = set
            }

            override fun onDenied(permissions: Permission) {
                if (Manifest.permission.READ_LOGS == permissions.name) {
                    //忽略Manifest.permission.READ_LOGS 系统权限
                    return
                }
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
        Log.i("onCreate $this")
    }

//    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
//        super.onCreate(savedInstanceState, persistentState)
//        Log.i("onCreate 2 $this")
//    }
//
//    override fun onAttachedToWindow() {
//        super.onAttachedToWindow()
//        Log.i("onAttachedToWindow $this")
//    }
//
//    override fun onTrimMemory(level: Int) {
//        super.onTrimMemory(level)
//        Log.i("onTrimMemory $this")
//    }
//
//    override fun onAttachFragment(fragment: Fragment?) {
//        super.onAttachFragment(fragment)
//        Log.i("onAttachFragment $this  $fragment")
//    }
//
//    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {
//        super.onSaveInstanceState(outState, outPersistentState)
//        Log.i("onSaveInstanceState 2 $this")
//    }
//
//    override fun onRestart() {
//        super.onRestart()
//        Log.i("onRestart $this")
//    }
//
//    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
//        super.onRestoreInstanceState(savedInstanceState)
//        Log.i("onRestoreInstanceState $this")
//    }
//
//    override fun onRestoreInstanceState(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
//        super.onRestoreInstanceState(savedInstanceState, persistentState)
//        Log.i("onRestoreInstanceState 2 $this")
//    }
//
//    override fun onStart() {
//        super.onStart()
//        Log.i("onStart $this")
//    }
//
//    override fun onResume() {
//        super.onResume()
//        Log.i("onResume $this")
//    }
//
//    override fun onPause() {
//        super.onPause()
//        Log.i("onPause $this")
//    }
//
//    override fun onStop() {
//        super.onStop()
//        Log.i("onStop $this")
//    }

    override fun onDestroy() {
        super.onDestroy()
        Session.activitys.remove(this)
        Log.i("onDestroy $this")
    }


    fun Disposable.destroy(onceKey: String? = null) {
        destroy(onceKey, lifecycle)
    }

    fun Disposable.stop(onceKey: String? = null) {
        stop(onceKey, lifecycle)
    }

    fun Disposable.pause(onceKey: String? = null) {
        pause(onceKey, lifecycle)
    }
}
package com.sjj.umeng

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.umeng.analytics.MobclickAgent
import com.umeng.commonsdk.UMConfigure
import com.umeng.commonsdk.statistics.common.DeviceConfig

private lateinit var app: Application

fun initUM(application: Application) {

    Log.e("um", getTestDeviceInfo(application).toList().toString())

    app = application

    setLogEnabled(true)

    UMConfigure.init(application, "5cc5607e0cafb22b38000495", "master", UMConfigure.DEVICE_TYPE_PHONE, "10b0e60c8c09a9ed26f1ef3751d3940e")
    MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.AUTO)
//    application.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
//        override fun onActivityPaused(activity: Activity?) {
//            MobclickAgent.onPause(activity);
//        }
//
//        override fun onActivityResumed(activity: Activity?) {
//            MobclickAgent.onResume(activity);
//        }
//
//        override fun onActivityStarted(activity: Activity?) {
//
//        }
//
//        override fun onActivityDestroyed(activity: Activity?) {
//        }
//
//        override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {
//        }
//
//        override fun onActivityStopped(activity: Activity?) {
//        }
//
//        override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
//            if (activity is FragmentActivity) {
////                activity.supportFragmentManager.registerFragmentLifecycleCallbacks(object : FragmentManager.FragmentLifecycleCallbacks() {
////                    override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
////                        super.onFragmentResumed(fm, f)
////                        Log.e("um onFragmentResumed",f.tag ?: f.javaClass.simpleName)
////                        MobclickAgent.onPageStart(f.tag ?: f.javaClass.simpleName)
////                    }
////
////                    override fun onFragmentPaused(fm: FragmentManager, f: Fragment) {
////                        super.onFragmentPaused(fm, f)
////                        Log.e("um onFragmentPaused",f.tag ?: f.javaClass.simpleName)
////                        MobclickAgent.onPageEnd(f.tag ?: f.javaClass.simpleName)
////                    }
////                }, true)
//            }
//        }
//    })
}

/**
 * 友盟日志开关
 */
fun setLogEnabled(boolean: Boolean) {
    UMConfigure.setLogEnabled(boolean)
}

fun getTestDeviceInfo(context: Context?): Array<String?> {
    val deviceInfo = arrayOfNulls<String>(2)
    try {
        if (context != null) {
            deviceInfo[0] = DeviceConfig.getDeviceIdForGeneral(context)
            deviceInfo[1] = DeviceConfig.getMac(context)
        }
    } catch (e: Exception) {
    }

    return deviceInfo
}

fun onPageStart(string: String) {
    MobclickAgent.onPageStart(string)
}

fun onPageEnd(string: String) {
    MobclickAgent.onPageEnd(string)
}

fun onEvent(key: String) {
    MobclickAgent.onEvent(app, key)
}
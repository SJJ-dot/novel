package sjj.novel

import android.app.Activity
import android.app.Application
import android.os.Bundle
import sjj.alog.Log

object ActivityLifecycle {
    fun setActivityLifecycleCallback(app: Application) {
        app.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityPaused(activity: Activity?) {
                Log.i("activity:$activity")
            }

            override fun onActivityResumed(activity: Activity?) {
                Log.i("activity:$activity")
            }

            override fun onActivityStarted(activity: Activity?) {
                Log.i("activity:$activity")
            }

            override fun onActivityDestroyed(activity: Activity?) {
                Log.i("activity:$activity")
            }

            override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {
                Log.i("activity:$activity")
            }

            override fun onActivityStopped(activity: Activity?) {
                Log.i("activity:$activity")
            }

            override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
                Log.i("activity:$activity")
            }
        })
    }
}
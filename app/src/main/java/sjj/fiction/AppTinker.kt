package sjj.fiction

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.StrictMode
import android.support.multidex.MultiDex
import com.facebook.drawee.backends.pipeline.Fresco
import io.reactivex.plugins.RxJavaPlugins
import org.jetbrains.anko.newTask
import org.jetbrains.anko.noHistory
import sjj.alog.Config
import sjj.alog.Log
import java.util.*

/**
 * Created by SJJ on 2017/9/3.
 */
//@DefaultLifeCycle(application = "sjj.fiction.App", flags = ShareConstants.TINKER_ENABLE_ALL, loadVerifyFlag = false)
class AppTinker:Application() {
    companion object {
        lateinit var app: AppTinker
    }


    override fun onCreate() {
        super.onCreate()
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            Log.e("UncaughtException",e)
            Session.finishAllActivity()
            val intent = Intent(Session.ctx, CrashActivity::class.java)
            intent.putExtra(CrashActivity.THREAD_INFO, "线程：${t.name} ID：${t.id}")
            intent.putExtra(CrashActivity.CRASH_DATA, android.util.Log.getStackTraceString(e))
            intent.noHistory()
            Session.ctx.startActivity(intent)
            System.exit(0)
        }
        app = this
        Session.ctx = this


//        // 我们可以从这里获得Tinker加载过程的信息
//       val tinkerApplicationLike = TinkerPatchApplicationLike.getTinkerPatchApplicationLike();
//
//        // 初始化TinkerPatch SDK, 更多配置可参照API章节中的,初始化SDK
//        TinkerPatch.init(tinkerApplicationLike)
//                .reflectPatchLibrary()
//                .setPatchRollbackOnScreenOff(true)
//                .setPatchRestartOnSrceenOff(true)
//                .setFetchPatchIntervalByHours(1);
//
//        // 每隔3个小时(通过setFetchPatchIntervalByHours设置)去访问后台时候有更新,通过handler实现轮训的效果
//        TinkerPatch.with().fetchPatchUpdateAndPollWithInterval();


//        initTinker()


        val logConfig = Config()
        logConfig.hold = true
        logConfig.holdMultiple = false
        logConfig.holdLev = Config.ERROR
        Config.init(logConfig)

        Fresco.initialize(Session.ctx);

        // 分别为MainThread和VM设置Strict Mode
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()

                    //                    .detectResourceMismatches()
                    .detectCustomSlowCalls().detectAll()
                    .penaltyLog()
//                    .penaltyDeath()
                    .build())

            StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .detectLeakedRegistrationObjects()
                    .detectActivityLeaks().detectAll()
                    .penaltyLog()
//                    .penaltyDeath()
                    .build())
        }

        RxJavaPlugins.setErrorHandler { Log.e("error $it",it) }
    }

    fun exit() {
        System.exit(0)
    }




    private fun initTinker() {
//        if (BuildConfig.TINKER_ENABLE) {
            //开始检查是否有补丁，这里配置的是每隔访问3小时服务器是否有更新。
//            TinkerPatch.init(this)
//                    .reflectPatchLibrary()
//                    .setPatchRollbackOnScreenOff(true)
//                    .setPatchRestartOnSrceenOff(true)
//                    .setFetchPatchIntervalByHours(3)
//
//            // 获取当前的补丁版本
//            Log.e("current patch version is " + TinkerPatch.with().patchVersion!!)
//
//            //每隔3个小时去访问后台时候有更新,通过handler实现轮训的效果
//            TinkerPatch.with().fetchPatchUpdateAndPollWithInterval()
//        }
    }

}
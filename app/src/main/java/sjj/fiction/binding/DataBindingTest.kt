package sjj.fiction.binding

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.util.Pools
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import sjj.alog.Log
import sjj.fiction.BaseActivity
import sjj.fiction.R
import sjj.fiction.Session
import sjj.fiction.databinding.ActivityDataBindingTestBinding
import sjj.fiction.model.User
import sjj.fiction.util.log
import sjj.fiction.util.submit

/**
 * Created by sjj on 2017/12/20.
 */
class DataBindingTest : BaseActivity() {
    val user = Session.user
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val aaa = DataBindingUtil.setContentView<ActivityDataBindingTestBinding>(this, R.layout.activity_data_binding_test)
        user.name.set("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
        aaa.user = user
        Thread.currentThread().log()
        submit(Schedulers.computation(),{
            Thread.currentThread().log()
            user.name.set("sssssssssssssssssssssssssssss")
        },3000)
        submit(Schedulers.computation(),{
            Log.e("aaaaa${user}   ")
        },6000)
    }
}
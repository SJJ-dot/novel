package sjj.fiction

import android.support.v4.app.Fragment
import io.reactivex.disposables.Disposable
import sjj.fiction.util.destroy
import sjj.fiction.util.pause
import sjj.fiction.util.stop

/**
 * Created by SJJ on 2017/10/7.
 */
open class BaseFragment : Fragment() {
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
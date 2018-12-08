package sjj.novel

import android.support.v4.app.Fragment
import io.reactivex.disposables.Disposable
import sjj.novel.util.destroy
import sjj.novel.util.pause
import sjj.novel.util.stop

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
package sjj.novel.util

import android.os.Looper
import androidx.lifecycle.MutableLiveData

open class SafeLiveData<T> : MutableLiveData<T>() {
    override fun setValue(value: T) {
        if (Looper.getMainLooper().thread == Thread.currentThread()) {
            super.setValue(value)
        } else {
            super.postValue(value)
        }
    }

    override fun postValue(value: T) {
        setValue(value)
    }
}
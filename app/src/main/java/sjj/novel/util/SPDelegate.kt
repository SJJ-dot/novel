package sjj.novel.util

import android.arch.lifecycle.MutableLiveData
import android.content.SharedPreferences
import android.os.Looper
import com.tencent.mmkv.MMKV
import org.jetbrains.anko.db.NULL
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.jvmErasure

@Suppress("IMPLICIT_CAST_TO_ANY", "UNCHECKED_CAST")
class DelegateSharedPreferences<T>(private val def: T, private val k: String? = null, val sp: () -> SharedPreferences? = { MMKV.defaultMMKV() }) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {

        val key: String = k ?: property.name

        val sp = sp() ?: return def

        return when (property.returnType.classifier) {
            String::class -> sp.getString(key, def as? String)
            Boolean::class -> sp.getBoolean(key, def as Boolean)
            Float::class -> sp.getFloat(key, def as Float)
            Int::class -> sp.getInt(key, def as Int)
            Long::class -> sp.getLong(key, def as Long)
            Set::class -> sp.getStringSet(key, def as? Set<String>)
            else -> throw IllegalArgumentException("only support String、Boolean、Float、Int、Long、Set<String>")
        } as T
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        val edit = sp()?.edit() ?: return

        val key: String = k ?: property.name

        when (property.returnType.classifier) {
            String::class -> edit.putString(key, value as? String)
            Boolean::class -> edit.putBoolean(key, value as Boolean)
            Float::class -> edit.putFloat(key, value as Float)
            Int::class -> edit.putInt(key, value as Int)
            Long::class -> edit.putLong(key, value as Long)
            Set::class -> edit.putStringSet(key, value as? Set<String>)
            else -> throw IllegalArgumentException("only support String、Boolean、Float、Int、Long、Set<String>")
        }
        edit.apply()
    }
}


/**
 * only support String、Boolean、Float、Double、Int、Long、Set<String>、ByteArray
 * mmkv Set<String> 不允许为空
 */
class DelegateLiveData<T>(private val def: T,
                          private val k: String? = null,
                          val sp: () -> SharedPreferences? = { MMKV.defaultMMKV() },
                          val toString: ((T) -> String)? = null,
                          val fromString: ((String?) -> T)? = null) {
    private var liveData: MutableLiveData<T>? = null

    @Synchronized
    operator fun getValue(thisRef: Any?, property: KProperty<*>): MutableLiveData<T> {
        if (liveData == null) {
            liveData = HoldLiveData(def, k ?: property.name, property, sp, toString, fromString)
        }
        return liveData!!
    }
}

/**
 *
 * only support String、Boolean、Float、Double、Int、Long、Set<String>、ByteArray
 */
@Suppress("IMPLICIT_CAST_TO_ANY", "UNCHECKED_CAST")
class HoldLiveData<T>(private val def: T,
                      private val key: String,
                      private val property: KProperty<*>,
                      val sp: () -> SharedPreferences?,
                      val toString: ((T) -> String)? = null, val fromString: ((String?) -> T)? = null) : MutableLiveData<T>() {

    init {

        if (Thread.currentThread() == Looper.getMainLooper().thread) {
            super.setValue(initValue())
        } else {
            super.postValue(initValue())
        }
    }

    /**
     * 使用Java的class 类型判断需要的返回值
     */
    private fun initValue() = when (property.returnType.arguments[0].type!!.jvmErasure.java) {
        String::class.java -> sp()?.getString(key, def as? String)
        Boolean::class.java -> sp()?.getBoolean(key, def as Boolean)
        Float::class.java -> sp()?.getFloat(key, def as Float)
        Int::class.java -> sp()?.getInt(key, def as Int)
        Long::class.java -> sp()?.getLong(key, def as Long)
        Set::class.java -> sp()?.getStringSet(key, def as? Set<String>)
        else -> fromString!!(sp()?.getString(key, toString!!(def)))
    } as? T

    /**
     * 保存数据到 SharedPreferences 中
     */
    private fun saveValue(value: T) {
        val edit = sp()?.edit()
        when (property.returnType.arguments[0].type!!.jvmErasure.java) {
            String::class.java -> edit?.putString(key, value as? String)
            Boolean::class.java -> edit?.putBoolean(key, value as Boolean)
            Float::class.java -> edit?.putFloat(key, value as Float)
            Int::class.java -> edit?.putInt(key, value as Int)
            Long::class.java -> edit?.putLong(key, value as Long)
            Set::class.java -> edit?.putStringSet(key, value as? Set<String>)
            else -> edit?.putString(key, toString!!(value))
        }
        edit?.apply()


        //通知 live data 更新
        if (Thread.currentThread() == Looper.getMainLooper().thread) {
            super.setValue(value)
        } else {
            super.postValue(value)
        }

    }


    override fun postValue(value: T) {
        saveValue(value)
    }

    override fun setValue(value: T) {
        saveValue(value)
    }

}
package sjj.fiction.util

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.content.SharedPreferences
import android.os.Looper
import com.google.gson.reflect.TypeToken
import sjj.fiction.App
import java.lang.reflect.Type
import kotlin.reflect.KProperty

val sharedPreferences by lazy { App.app.getSharedPreferences("generalDelegate", Context.MODE_PRIVATE) }

inline fun <reified T : Any> sharedPreferencesDelegate(def: T?, noinline sp: () -> SharedPreferences = { sharedPreferences }) = SharedPreferencesDelegate(def, sp, type = object : TypeToken<T>() {}.type)

class SharedPreferencesDelegate<T>(private val def: T?, val sp: () -> SharedPreferences = { sharedPreferences }, val type: Type) {
    private val json by lazy { gson.toJson(def) }
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        val sp = sp()
        return when (type) {
            String::class.java -> sp.getString(property.name, def as String?)
            Boolean::class.java -> sp.getBoolean(property.name, def as Boolean)
            Float::class.java -> sp.getFloat(property.name, def as Float)
            Int::class.java -> sp.getInt(property.name, def as Int)
            Long::class.java -> sp.getLong(property.name, def as Long)
            else -> gson.fromJson(sp.getString(property.name, json), type)
        } as T
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
        val edit = sp().edit()
        when (type) {
            String::class.java -> edit.putString(property.name, value as String?)
            Boolean::class.java -> edit.putBoolean(property.name, value as Boolean)
            Float::class.java -> edit.putFloat(property.name, value as Float)
            Int::class.java -> edit.putInt(property.name, value as Int)
            Long::class.java -> edit.putLong(property.name, value as Long)
            else -> edit.putString(property.name, gson.toJson(value))
        }
        edit.apply()
    }
}

inline fun <reified T : Any> liveDataDelegate(def: T?, noinline sp: () -> SharedPreferences = { sharedPreferences }) = SharedPreferencesLiveData(def, sp, type = object : TypeToken<T>() {}.type)

class SharedPreferencesLiveData<T>(val def: T?, val sp: () -> SharedPreferences = { sharedPreferences }, val type: Type) {
    private val json by lazy { gson.toJson(def) }
    private var liveData: MutableLiveData<T>? = null
    operator fun getValue(thisRef: Any?, property: KProperty<*>): MutableLiveData<T> {
        if (liveData == null) {
            liveData = object : MutableLiveData<T>() {
                override fun setValue(value: T) {
                    val edit = sp().edit()
                    when (type) {
                        String::class.java -> edit.putString(property.name, value as String?)
                        Boolean::class.java -> edit.putBoolean(property.name, value as Boolean)
                        Float::class.java -> edit.putFloat(property.name, value as Float)
                        Int::class.java -> edit.putInt(property.name, value as Int)
                        Long::class.java -> edit.putLong(property.name, value as Long)
                        else -> edit.putString(property.name, gson.toJson(value))
                    }
                    edit.apply()
                    if (Thread.currentThread() == Looper.getMainLooper().thread) {
                        super.setValue(value)
                    } else {
                        super.postValue(value)
                    }
                }
            }
            liveData?.value = when (type) {
                String::class.java -> sp().getString(property.name, def as String?)
                Boolean::class.java -> sp().getBoolean(property.name, def as Boolean)
                Float::class.java -> sp().getFloat(property.name, def as Float)
                Int::class.java -> sp().getInt(property.name, def as Int)
                Long::class.java -> sp().getLong(property.name, def as Long)
                else -> gson.fromJson(sp().getString(property.name, json), type)
            } as T
        }
        return liveData!!
    }
}
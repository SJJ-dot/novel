package sjj.fiction;

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import sjj.alog.Log

/**
 * Created by SJJ on 2017/10/12.
 * APP配置
 */

class Configuration(val context: Context) {
    private val generalSp: SharedPreferences = context.getSharedPreferences("General", Context.MODE_PRIVATE)
    private val generalEditor: SharedPreferences.Editor = generalSp.edit()
    var userSp: SharedPreferences = generalSp
    var userEditor: SharedPreferences.Editor = generalEditor
    fun initUserConfig(username: String) {
        userSp = context.getSharedPreferences(username, Context.MODE_PRIVATE);
        userEditor = userSp.edit()
    }

    fun setHttp302Url(url: String, newUrl: String, vararg parameters: String) {
        val builder = StringBuilder(url).append("?")
        for (s in parameters) {
            builder.append(s).append("&")
        }
        builder.deleteCharAt(builder.length - 1)
        userEditor.putString(builder.toString(), newUrl).commit()
    }

    fun getHttp302Url(url: String, vararg parameters: String): String {
        val builder = StringBuilder(url).append("?")
        for (s in parameters) {
            builder.append(s).append("&")
        }
        builder.deleteCharAt(builder.length - 1)
        return userSp.getString(builder.toString(), "")
    }

//    fun getUserSp() = userSp
//    fun set(stringArray: List<String>) {
//        userEditor.putString(type_string_array, gson.toJson(stringArray)).commit()
//    }
//
//    fun <T> get(): T? {
//        val string = userSp.getString(type_string_array, "")
//        val value: TypeToken<T> = object : TypeToken<T>() {}
//        val type = value.type
//        Log.e(value.rawType)
//        return gson.fromJson<T>(string, type)
//    }
}

package sjj.fiction;

import android.content.Context
import android.content.SharedPreferences
import sjj.alog.Log

/**
 * Created by SJJ on 2017/10/12.
 * APP配置
 */

class Configuration(val context: Context) {
    private val generalSp: SharedPreferences = context.getSharedPreferences("General", Context.MODE_PRIVATE)
    private val generalEditor: SharedPreferences.Editor = generalSp.edit()
    private var userSp: SharedPreferences = generalSp
    private var userEditor: SharedPreferences.Editor = generalEditor

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

}

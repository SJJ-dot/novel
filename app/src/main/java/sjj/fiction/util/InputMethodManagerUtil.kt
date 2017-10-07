package sjj.fiction.util

import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.view.View
import android.view.inputmethod.InputMethodManager
import sjj.fiction.App


/**
 * Created by SJJ on 2017/10/7.
 */
fun showSoftInput(forcedView:View) {
    val inputMethodManager = App.app.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.showSoftInput(forcedView, InputMethodManager.SHOW_FORCED)
}

fun hideSoftInput(v: View) {
    val imm = App.app.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(v.windowToken, 0)
}
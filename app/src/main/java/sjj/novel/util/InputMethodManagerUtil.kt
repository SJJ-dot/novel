package sjj.novel.util

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import sjj.novel.Session


/**
 * Created by SJJ on 2017/10/7.
 */
fun showSoftInput(forcedView:View) {
    val inputMethodManager = Session.ctx.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.showSoftInput(forcedView, InputMethodManager.SHOW_FORCED)
}

fun hideSoftInput(v: View) {
    val imm = Session.ctx.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(v.windowToken, 0)
}
package sjj.novel.util

import sjj.novel.Session

val Int.resStr
    get() = Session.ctx.getString(this)

fun Int.resStr(vararg formatArgs: Any?): String {
    return Session.ctx.getString(this,*formatArgs)
}
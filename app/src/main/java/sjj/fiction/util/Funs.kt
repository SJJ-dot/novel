package sjj.fiction.util

import java.util.regex.Pattern

/**
 * Created by SJJ on 2017/10/15.
 */

fun String.domain(): String {
    val pattern = "(http(s)?://[a-zA-z\\d.]++)/?"
    val r = Pattern.compile(pattern)
    val m = r.matcher(this)
    if (m.find()) {
        return m.group(1)
    }
    return "error"
}

fun Throwable.stackTraceString(): String {
    val buffer = StringBuilder()
    buffer.append(this::class.java).append(", ").append(message).append("\n")
    var throwable: Throwable? = this
    while (throwable != null) {
        for (element in throwable.stackTrace) {
            buffer.append(element.toString()).append("\n")
        }
        throwable = throwable.cause
        if (throwable != null)
            buffer.append("caused by ")
    }
    return buffer.toString()
}
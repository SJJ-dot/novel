package sjj.fiction.model

import java.util.regex.Pattern
import kotlin.properties.Delegates

/**
 * Created by sjj on 2017/9/22.
 */
data class Url(val url: String) {
    private val error = "error"
    val domain by lazy {
        val pattern = "(http(s)?://[a-zA-z\\d.]++)/?"
        val r = Pattern.compile(pattern)
        val m = r.matcher(url)
        if (m.find()) {
            return@lazy m.group(1)
        }
        return@lazy error
    }
    val valid = domain != error
}
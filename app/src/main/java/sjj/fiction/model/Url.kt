package sjj.fiction.model

import java.io.Serializable
import java.util.regex.Pattern
import kotlin.properties.Delegates

/**
 * Created by sjj on 2017/9/22.
 */
data class Url(var url: String = "") : Serializable {
    companion object {
        val def = Url()
    }

    private val error = "error"
    fun domain(): Url = Url(domainLazy.value)

    private val domainLazy = lazy {
        val pattern = "(http(s)?://[a-zA-z\\d.]++)/?"
        val r = Pattern.compile(pattern)
        val m = r.matcher(url)
        if (m.find()) {
            return@lazy m.group(1)
        }
        return@lazy error
    }
}
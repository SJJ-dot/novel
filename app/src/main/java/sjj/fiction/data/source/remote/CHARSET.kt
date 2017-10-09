package sjj.fiction.data.source.remote

import java.nio.charset.Charset

/**
 * Created by SJJ on 2017/10/10.
 */
@Target(AnnotationTarget.FUNCTION)
annotation class CHARSET(val charset: String = "utf-8")
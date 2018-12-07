package sjj.fiction

import org.junit.Test

import org.junit.Assert.*
import sjj.alog.Log

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }


    @Test
    fun regexTest() {
        val regex = Regex("\\d")
        val result = regex.find("absc1lskdk122kjj3")
        Log.e(result?.groupValues)
    }

}

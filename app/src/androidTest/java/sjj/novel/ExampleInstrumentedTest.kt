package sjj.novel

import androidx.test.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import sjj.alog.Log
import sjj.novel.data.repository.novelDataRepository

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getTargetContext()
        assertEquals("sjj.novel", appContext.packageName)
    }

    @Test
    fun regexTest() {
        val regex = Regex("(.*)/.*")
        val result = regex.find("喵星人家的汪/玄幻奇幻")
        Log.e("result?.groupValues "+result?.groups?.get(1)?.value)
        Log.e("result?.groupValues "+result?.groupValues?.get(1))
    }

    @Test
    fun fictionDataRepositoryTest() {
        //网络请求似乎不行
        novelDataRepository.search("哈利波特").subscribe({
            Log.e(it)
        },{

            Log.e(it)
        })
    }

}

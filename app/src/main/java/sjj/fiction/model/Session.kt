package sjj.fiction.model

import java.util.HashMap

/**
 * Created by sjj on 2017/8/2.
 */

class Session<D> {
    private val objectMap = HashMap<String, D>()
    operator fun set(key: String, o: D) {
        objectMap.put(key, o)
    }

    operator fun <T : D> get(key: String): T {
        return objectMap[key] as T
    }

    fun <T : D> remove(key: String): T {
        return objectMap.remove(key) as T
    }

    fun clear(): Map<String, D> {
        val temp = HashMap(objectMap)
        objectMap.clear()
        return temp
    }
}

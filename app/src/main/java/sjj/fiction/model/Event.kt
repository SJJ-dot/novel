package sjj.fiction.model

/**
 * Created by SJJ on 2017/11/25.
 */
data class Event(val id: Int, val value: Any) {
    companion object {
        const val NEW_BOOK = 1
    }
}
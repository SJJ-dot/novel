package sjj.novel.util

/**
 * Created by sjj on 2017/9/19.
 */
fun ByteArray.toHexString(): String = map { String.format("%02X ", it) }.reduce { acc, s -> acc + " " + s }

fun ByteArray.hex(): String = map { String.format("%02X ", it) }.reduce { acc, s -> acc + s }

fun ByteArray.toHexString(start: Int, length: Int): String {
    val sb = StringBuilder()
    for (index in start until start + length) {
        sb.append(String.format("%02X ", get(index)))
    }
    return sb.toString()
}

fun Int.hex() = String.format("%02X ", this)
fun Long.hex() = String.format("%02X ", this)
fun print() {
    val sb = java.lang.StringBuilder()
    val id = 1L
    for (i in 0 until 64) {
        sb.append(id shr i and 0x01).append("-")
    }
    sb.log()
}
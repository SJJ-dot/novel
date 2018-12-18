package sjj.novel.util

import java.util.zip.CRC32

val String.id:Long
    get() {
        val crC32 = CRC32()
        crC32.update(this.toByteArray())
        return this.hashCode().toLong() shl 32 or crC32.value
    }
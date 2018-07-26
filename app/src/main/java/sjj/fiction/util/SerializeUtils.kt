package sjj.fiction.util

import android.util.Base64
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

fun Any?.serialize(): String {
    var stream: ByteArrayOutputStream? = null
    var objs: ObjectOutputStream? = null
    try {
        stream = ByteArrayOutputStream()
        objs = ObjectOutputStream(stream)
        objs.writeObject(this)
        objs.flush()
        return Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT)
    } finally {
        stream?.close()
        objs?.close()
    }

}

fun <T> String.deSerialize(): T {
    var inputStream: ObjectInputStream? = null
    try {
        inputStream = ObjectInputStream(ByteArrayInputStream(Base64.decode(this, Base64.DEFAULT)))
        return inputStream.readObject() as T
    } finally {
        inputStream?.close()
    }
}
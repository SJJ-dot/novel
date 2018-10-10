package sjj.fiction.util

import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.SecretKeySpec


private val cipher = Cipher.getInstance("RC4");
private val key = SecretKeySpec("密钥".toByteArray(), "RC4");
//val key = KeyGenerator.getInstance("RC4").generateKey()
fun ByteArray.encrypt(): ByteArray {
    cipher.init(Cipher.ENCRYPT_MODE, key)
    return cipher.update(this)
}

fun ByteArray.decrypt(): ByteArray {
    cipher.init(Cipher.DECRYPT_MODE, key)
    return cipher.update(this)
}
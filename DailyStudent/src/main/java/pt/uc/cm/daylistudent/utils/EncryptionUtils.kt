package pt.uc.cm.daylistudent.utils

import android.util.Base64
import java.security.MessageDigest
import java.util.*
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

class EncryptionUtils private constructor() {

    private var encryptionKey: String? = null

    private object Holder {
        val INSTANCE = EncryptionUtils()
    }

    companion object {
        val instance: EncryptionUtils by lazy { Holder.INSTANCE }
    }

    @Throws(Exception::class)
    fun getSecreteKey(secretKey: String): SecretKey {
        val md = MessageDigest.getInstance("SHA-1")
        val digestOfPassword = md.digest(secretKey.toByteArray(charset("UTF-8")))
        val keyBytes = Arrays.copyOf(digestOfPassword, 24)
        return SecretKeySpec(keyBytes, "AES")
    }

    fun encryptMsg(): String {
        return Base64.encodeToString(encryptionKey!!.toByteArray(), Base64.DEFAULT)
    }

    fun encryptionString(encryptionKey: String): EncryptionUtils? {
        this.encryptionKey = encryptionKey
        return this
    }

    fun getDecryptionString(encryptedText: String): String {
        return String(Base64.decode(encryptedText.toByteArray(), Base64.DEFAULT))
    }
}

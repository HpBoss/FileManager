package com.baidu.duer.files.util

import java.security.MessageDigest

fun ByteArray.sha1Digest(): ByteArray = MessageDigest.getInstance("SHA-1").digest(this)

fun ByteArray.toHexString(): String {
    val chars = CharArray(2 * size)
    for (index in indices) {
        val byte = this[index]
        chars[2 * index] = ((byte.toInt() ushr 4) and 0xF).toHexChar()
        chars[2 * index + 1] = (byte.toInt() and 0xF).toHexChar()
    }
    return String(chars)
}

private fun Int.toHexChar(): Char = if (this >= 10) 'a' + (this - 10) else '0' + this

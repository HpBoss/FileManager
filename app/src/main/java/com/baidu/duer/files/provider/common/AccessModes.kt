package com.baidu.duer.files.provider.common

import java8.nio.file.AccessMode

class AccessModes(val read: Boolean, val write: Boolean, val execute: Boolean)

fun Array<out AccessMode>.toAccessModes(): AccessModes {
    var read = false
    var write = false
    var execute = false
    for (mode in this) {
        when (mode) {
            AccessMode.READ -> read = true
            AccessMode.WRITE -> write = true
            AccessMode.EXECUTE -> execute = true
            else -> throw UnsupportedOperationException(mode.toString())
        }
    }
    return AccessModes(read, write, execute)
}

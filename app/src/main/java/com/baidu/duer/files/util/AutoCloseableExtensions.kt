package com.baidu.duer.files.util

fun AutoCloseable.closeSafe() {
    try {
        close()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

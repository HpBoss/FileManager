package com.baidu.duer.files.util

fun Any.hash(vararg values: Any?): Int = values.contentDeepHashCode()

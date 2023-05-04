package com.baidu.duer.files.util

import kotlinx.coroutines.flow.MutableStateFlow

@Suppress("FunctionName", "UNCHECKED_CAST")
fun <T : Any> LateInitMutableStateFlow(): MutableStateFlow<T> =
    MutableStateFlow<T?>(null) as MutableStateFlow<T>

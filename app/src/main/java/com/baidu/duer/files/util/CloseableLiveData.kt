package com.baidu.duer.files.util

import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import java.io.Closeable

abstract class CloseableLiveData<T> : LiveData<T>, Closeable {
    protected var coroutineScope: CoroutineScope? = null

    @Volatile
    protected var isChangedWhileInactive = false

    constructor(value: T) : super(value)

    constructor()

    abstract fun loadValue()

    override fun onActive() {
        if (isChangedWhileInactive) {
            loadValue()
            isChangedWhileInactive = false
        }
    }

    override fun close() {
        coroutineScope?.cancel()
    }
}

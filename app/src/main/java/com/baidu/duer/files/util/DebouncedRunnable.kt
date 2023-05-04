package com.baidu.duer.files.util

import android.os.Handler

class DebouncedRunnable(
    private val handler: Handler,
    private val intervalMillis: Long,
    block: () -> Unit
) : () -> Unit {
    private val lock = Any()

    private val runnable = Runnable(block)

    override operator fun invoke() {
        synchronized(lock) {
            handler.removeCallbacks(runnable)
            handler.postDelayed(runnable, intervalMillis)
        }
    }

    fun cancel() {
        synchronized(lock) { handler.removeCallbacks(runnable) }
    }
}

package com.baidu.duer.files.util

import android.os.CancellationSignal
import kotlinx.coroutines.*
import kotlin.coroutines.resume

// @see androidx.room.CoroutinesRoom.execute
suspend fun <T> runWithCancellationSignal(block: (CancellationSignal) -> T): T {
    val signal = CancellationSignal()
    return suspendCancellableCoroutine { continuation ->
        @OptIn(DelicateCoroutinesApi::class)
        val job = GlobalScope.launch(Dispatchers.IO) {
            continuation.resume(block(signal))
        }
        continuation.invokeOnCancellation {
            signal.cancel()
            job.cancel()
        }
    }
}

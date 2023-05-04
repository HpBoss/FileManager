package com.baidu.duer.files.filejob

import android.util.Log
import java.io.IOException
import java.io.InterruptedIOException
import java.util.*

abstract class FileJob {
    val id = Random().nextInt()

    internal lateinit var service: FileJobService
        private set

    fun runOn(service: FileJobService) {
        this.service = service
        try {
            run()
            // TODO: Toast
        } catch (e: InterruptedIOException) {
            // TODO
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.i("FileJob", "exception: $e")
        } finally {
            service.notificationManager.cancel(id)
        }
    }

    @Throws(IOException::class)
    protected abstract fun run()
}

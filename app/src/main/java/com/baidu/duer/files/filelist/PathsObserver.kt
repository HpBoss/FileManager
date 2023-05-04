package com.baidu.duer.files.filelist

import android.os.AsyncTask
import android.os.Handler
import android.os.Looper
import androidx.annotation.MainThread
import com.baidu.duer.files.provider.common.PathObservable
import com.baidu.duer.files.provider.common.observe
import com.baidu.duer.files.util.closeSafe
import java8.nio.file.Path
import java.io.Closeable
import java.io.IOException

/**
 * @Author : 何飘
 * @CreateTime : 2023/2/22
 * @Description : 针对同时对多个File path进行监听变化
 */
class PathsObserver(pathList: List<Path>, @MainThread onChange: () -> Unit) : Closeable {
    private var pathObservableList: ArrayList<PathObservable> = ArrayList()

    private var closed = false
    private val lock = Any()

    init {
        AsyncTask.THREAD_POOL_EXECUTOR.execute {
            synchronized(lock) {
                if (closed) {
                    return@execute
                }
                pathList.forEach {
                    val pathObservable = try {
                        it.observe(THROTTLE_INTERVAL_MILLIS)
                    } catch (e: UnsupportedOperationException) {
                        // Ignored.
                        return@execute
                    } catch (e: IOException) {
                        // Ignored.
                        e.printStackTrace()
                        return@execute
                    }.apply {
                        val mainHandler = Handler(Looper.getMainLooper())
                        addObserver { mainHandler.postDelayed(onChange, RENOVATE_CHANGE_INTERVAL) }
                    }
                    pathObservableList.add(pathObservable)
                }
            }
        }
    }

    override fun close() {
        AsyncTask.THREAD_POOL_EXECUTOR.execute {
            synchronized(lock) {
                if (closed) {
                    return@execute
                }
                closed = true
                pathObservableList.forEach {
                    it.closeSafe()
                }
            }
        }
    }

    companion object {
        private const val THROTTLE_INTERVAL_MILLIS = 1000L
        private const val RENOVATE_CHANGE_INTERVAL = 100L
    }
}

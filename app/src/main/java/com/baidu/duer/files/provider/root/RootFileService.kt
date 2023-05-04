package com.baidu.duer.files.provider.root

import android.annotation.SuppressLint
import android.content.Context
import android.os.Process
import android.util.Log
import com.baidu.duer.files.BuildConfig
import com.baidu.duer.files.provider.FileSystemProviders
import com.baidu.duer.files.provider.remote.RemoteFileService
import com.baidu.duer.files.provider.remote.RemoteInterface
import com.baidu.duer.files.util.lazyReflectedMethod

val isRunningAsRoot = Process.myUid() == 0

@SuppressLint("StaticFieldLeak")
lateinit var rootContext: Context
    private set

object RootFileService : RemoteFileService(
    RemoteInterface {
        if (SuiFileServiceLauncher.isSuiAvailable()) {
            SuiFileServiceLauncher.launchService()
        } else {
            LibSuFileServiceLauncher.launchService()
        }
    }
) {
    const val TIMEOUT_MILLIS = 15 * 1000L

    private val LOG_TAG = RootFileService::class.java.simpleName

    // Not actually restricted because there's no restriction when running as root.
    //@RestrictedHiddenApi
    private val activityThreadCurrentActivityThreadMethod by lazyReflectedMethod(
        "android.app.ActivityThread", "currentActivityThread"
    )

    //@RestrictedHiddenApi
    private val activityThreadGetSystemContextMethod by lazyReflectedMethod(
        "android.app.ActivityThread", "getSystemContext"
    )

    fun main() {
        Log.i(LOG_TAG, "Creating package context")
        rootContext = createPackageContext(BuildConfig.APPLICATION_ID)
        Log.i(LOG_TAG, "Installing file system providers")
        FileSystemProviders.install()
        FileSystemProviders.overflowWatchEvents = true
    }

    private fun createPackageContext(packageName: String): Context {
        val activityThread = activityThreadCurrentActivityThreadMethod.invoke(null)
        val systemContext = activityThreadGetSystemContextMethod.invoke(activityThread) as Context
        return systemContext.createPackageContext(
            packageName, Context.CONTEXT_IGNORE_SECURITY or Context.CONTEXT_INCLUDE_CODE
        )
    }
}

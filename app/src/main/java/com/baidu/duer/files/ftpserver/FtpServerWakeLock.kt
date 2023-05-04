package com.baidu.duer.files.ftpserver

import android.net.wifi.WifiManager
import android.os.PowerManager
import com.baidu.duer.files.app.powerManager
import com.baidu.duer.files.app.wifiManager

class FtpServerWakeLock {
    private val wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, LOCK_TAG)
        .apply { setReferenceCounted(false) }
    private val wifiLock =
        wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, LOCK_TAG)
            .apply { setReferenceCounted(false) }

    fun acquire() {
        wakeLock.acquire()
        wifiLock.acquire()
    }

    fun release() {
        wifiLock.release()
        wakeLock.release()
    }

    companion object {
        private val LOCK_TAG = FtpServerWakeLock::class.java.simpleName
    }
}

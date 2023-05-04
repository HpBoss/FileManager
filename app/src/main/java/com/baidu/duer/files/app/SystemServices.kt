package com.baidu.duer.files.app

import android.content.ClipboardManager
import android.content.ContentResolver
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.PowerManager
import android.os.storage.StorageManager
import android.view.inputmethod.InputMethodManager
import androidx.core.app.NotificationManagerCompat
import androidx.preference.PreferenceManager
import com.baidu.duer.files.compat.getSystemServiceCompat
import com.baidu.duer.files.compat.mainExecutorCompat
import java.util.concurrent.Executor

val appClassLoader: ClassLoader by lazy { AppProvider::class.java.classLoader }

val contentResolver: ContentResolver by lazy { application.contentResolver }

val defaultSharedPreferences: SharedPreferences by lazy {
    PreferenceManager.getDefaultSharedPreferences(application)
}

val mainExecutor: Executor by lazy { application.mainExecutorCompat }

val packageManager: PackageManager by lazy { application.packageManager }

val clipboardManager: ClipboardManager by lazy {
    application.getSystemServiceCompat(ClipboardManager::class.java)
}

val inputMethodManager: InputMethodManager by lazy {
    application.getSystemServiceCompat(InputMethodManager::class.java)
}

val notificationManager: NotificationManagerCompat by lazy {
    NotificationManagerCompat.from(application)
}

val powerManager: PowerManager by lazy {
    application.getSystemServiceCompat(PowerManager::class.java)
}

val storageManager: StorageManager by lazy {
    application.getSystemServiceCompat(StorageManager::class.java)
}

val wifiManager: WifiManager by lazy {
    application.getSystemServiceCompat(WifiManager::class.java)
}

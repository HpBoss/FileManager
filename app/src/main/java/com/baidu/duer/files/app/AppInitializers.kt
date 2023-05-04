package com.baidu.duer.files.app

import android.os.AsyncTask
import android.webkit.WebView
import com.baidu.duer.files.BuildConfig
import com.baidu.duer.files.coil.initializeCoil
import com.baidu.duer.files.filejob.fileJobNotificationTemplate
import com.baidu.duer.files.ftpserver.ftpServerServiceNotificationTemplate
import com.baidu.duer.files.hiddenapi.HiddenApi
import com.baidu.duer.files.provider.FileSystemProviders
import com.baidu.duer.files.settings.Settings
import com.baidu.duer.files.storage.FtpServerAuthenticator
import com.baidu.duer.files.storage.SftpServerAuthenticator
import com.baidu.duer.files.storage.SmbServerAuthenticator
import com.baidu.duer.files.storage.StorageVolumeListLiveData
import com.baidu.duer.files.theme.custom.CustomThemeHelper
import com.baidu.duer.files.theme.night.NightModeHelper
import com.facebook.stetho.Stetho
import com.jakewharton.threetenabp.AndroidThreeTen
import jcifs.context.SingletonContext
import java.util.*
import com.baidu.duer.files.provider.ftp.client.Client as FtpClient
import com.baidu.duer.files.provider.sftp.client.Client as SftpClient
import com.baidu.duer.files.provider.smb.client.Client as SmbClient

val appInitializers by lazy {
    listOf(
        ::disableHiddenApiChecks, ::initializeThreeTen,
        ::initializeWebViewDebugging, ::initializeStetho, ::initializeCoil,
        ::initializeFileSystemProviders, ::initializeLiveDataObjects,
        ::createNotificationChannels
    )
}

private fun disableHiddenApiChecks() {
    HiddenApi.disableHiddenApiChecks()
}

private fun initializeThreeTen() {
    AndroidThreeTen.init(application)
}

private fun initializeWebViewDebugging() {
    if (BuildConfig.DEBUG) {
        WebView.setWebContentsDebuggingEnabled(true)
    }
}

private fun initializeStetho() {
    Stetho.initializeWithDefaults(application)
}

private fun initializeFileSystemProviders() {
    FileSystemProviders.install()
    FileSystemProviders.overflowWatchEvents = true
    // SingletonContext.init() calls NameServiceClientImpl.initCache() which connects to network.
    AsyncTask.THREAD_POOL_EXECUTOR.execute {
        SingletonContext.init(
            Properties().apply {
                setProperty("jcifs.netbios.cachePolicy", "0")
                setProperty("jcifs.smb.client.maxVersion", "SMB1")
            }
        )
    }
    FtpClient.authenticator = FtpServerAuthenticator
    SftpClient.authenticator = SftpServerAuthenticator
    SmbClient.authenticator = SmbServerAuthenticator
}

private fun initializeLiveDataObjects() {
    // Force initialization of LiveData objects so that it won't happen on a background thread.
    StorageVolumeListLiveData.value
    Settings.FILE_LIST_DEFAULT_DIRECTORY.value
}

private fun initializeCustomTheme() {
    CustomThemeHelper.initialize(application)
}

private fun initializeNightMode() {
    NightModeHelper.initialize(application)
}

private fun createNotificationChannels() {
    notificationManager.createNotificationChannels(
        listOf(
            backgroundActivityStartNotificationTemplate.channelTemplate,
            fileJobNotificationTemplate.channelTemplate,
            ftpServerServiceNotificationTemplate.channelTemplate
        ).map { it.create(application) }
    )
}

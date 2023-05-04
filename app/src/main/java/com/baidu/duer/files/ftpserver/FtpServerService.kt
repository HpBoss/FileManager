package com.baidu.duer.files.ftpserver

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.annotation.WorkerThread
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.baidu.duer.files.compat.mainExecutorCompat
import com.baidu.duer.files.settings.Settings
import com.baidu.duer.files.util.showToast
import com.baidu.duer.files.util.valueCompat
import java.util.concurrent.Executors

class FtpServerService : Service() {
    private var state = State.STOPPED
        set(value) {
            field = value
            _stateLiveData.value = value
        }

    private lateinit var wakeLock: FtpServerWakeLock

    private val executorService = Executors.newSingleThreadExecutor()

    private var server: FtpServer? = null

    override fun onCreate() {
        super.onCreate()

        wakeLock = FtpServerWakeLock()
        executeStart()
    }

    override fun onBind(intent: Intent): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onDestroy() {
        super.onDestroy()

        executeStop()
        executorService.shutdown()
    }

    private fun executeStart() {
        if (state == State.STARTING || state == State.RUNNING) {
            return
        }
        wakeLock.acquire()
        FtpServerServiceNotification.startForeground(this)
        state = State.STARTING
        executorService.execute { doStart() }
    }

    private fun onStartError(exception: Exception) {
        state = State.STOPPED
        showToast(exception.toString())
        FtpServerServiceNotification.stopForeground(this)
        wakeLock.release()
        stopSelf()
    }

    private fun executeStop() {
        if (state == State.STOPPING || state == State.STOPPED) {
            return
        }
        state = State.STOPPING
        executorService.execute { doStop() }
        FtpServerServiceNotification.stopForeground(this)
        wakeLock.release()
    }

    @WorkerThread
    private fun postState(state: State) {
        mainExecutorCompat.execute { this.state = state }
    }

    @WorkerThread
    private fun doStart() {
        if (server != null) {
            return
        }
        val username: String?
        val password: String?
        if (Settings.FTP_SERVER_ANONYMOUS_LOGIN.valueCompat) {
            username = USERNAME_ANONYMOUS
            password = null
        } else {
            username = Settings.FTP_SERVER_USERNAME.valueCompat
            password = Settings.FTP_SERVER_PASSWORD.valueCompat
        }
        val port = Settings.FTP_SERVER_PORT.valueCompat
        val homeDirectory = Settings.FTP_SERVER_HOME_DIRECTORY.valueCompat
        val writable = Settings.FTP_SERVER_WRITABLE.valueCompat
        val server = FtpServer(username, password, port, homeDirectory, writable)
        this.server = server
        try {
            server.start()
        } catch (e: Exception) {
            e.printStackTrace()
            this.server = null
            mainExecutorCompat.execute { onStartError(e) }
            return
        }
        postState(State.RUNNING)
    }

    @WorkerThread
    private fun doStop() {
        val server = server ?: return
        server.stop()
        this.server = null
        postState(State.STOPPED)
    }

    companion object {
        const val USERNAME_ANONYMOUS = "anonymous"

        private val _stateLiveData = MutableLiveData(State.STOPPED)
        val stateLiveData: LiveData<State>
            get() = _stateLiveData

        fun start(context: Context) {
            ContextCompat.startForegroundService(
                context, Intent(context, FtpServerService::class.java)
            )
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, FtpServerService::class.java))
        }

        fun toggle(context: Context) {
            when (val state = _stateLiveData.valueCompat) {
                State.STARTING, State.STOPPING -> {}
                State.RUNNING -> stop(context)
                State.STOPPED -> start(context)
                else -> throw AssertionError(state)
            }
        }
    }

    enum class State {
        STARTING,
        RUNNING,
        STOPPING,
        STOPPED
    }
}

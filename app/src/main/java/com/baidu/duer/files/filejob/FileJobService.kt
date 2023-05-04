package com.baidu.duer.files.filejob

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.annotation.MainThread
import com.baidu.duer.files.file.MimeType
import com.baidu.duer.files.provider.common.PosixFileModeBit
import com.baidu.duer.files.provider.common.PosixGroup
import com.baidu.duer.files.provider.common.PosixUser
import com.baidu.duer.files.util.ForegroundNotificationManager
import com.baidu.duer.files.util.removeFirst
import java8.nio.file.Path
import java.util.concurrent.Executors
import java.util.concurrent.Future

class FileJobService : Service() {
    internal lateinit var notificationManager: ForegroundNotificationManager
        private set

    private val executorService = Executors.newCachedThreadPool()

    private val runningJobs = mutableMapOf<FileJob, Future<*>>()

    override fun onCreate() {
        super.onCreate()

        notificationManager = ForegroundNotificationManager(this)
        instance = this

        while (pendingJobs.isNotEmpty()) {
            startJob(pendingJobs.removeFirst())
        }
    }

    private fun startJob(job: FileJob) {
        // Synchronize on runningJobs to prevent a job from removing itself before being added.
        synchronized(runningJobs) {
            val future = executorService.submit {
                job.runOn(this)
                synchronized(runningJobs) { runningJobs.remove(job) }
            }
            runningJobs[job] = future
        }
    }

    override fun onBind(intent: Intent): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    private val jobCount: Int
        get() = synchronized(runningJobs) { runningJobs.size }

    private fun cancelJob(id: Int, operate: (() -> Unit)? = null) {
        synchronized(runningJobs) {
            runningJobs.removeFirst { it.key.id == id }?.value?.cancel(true)
            if (operate != null) {
                operate()
            }
        }
    }

    private fun cancelFirstJob(operate: (() -> Unit)? = null) {
        synchronized(runningJobs) {
            if (runningJobs.isNotEmpty()) {
                runningJobs.removeFirst().value.cancel(true)
                if (operate != null) {
                    operate()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        instance = null

        synchronized(runningJobs) {
            while (runningJobs.isNotEmpty()) {
                runningJobs.removeFirst().value.cancel(true)
            }
        }
    }

    companion object {
        private var instance: FileJobService? = null

        private val pendingJobs = mutableListOf<FileJob>()

        val runningJobCount: Int
            @MainThread
            get() = instance?.jobCount ?: 0

        @MainThread
        private fun startJob(job: FileJob, context: Context) {
            val instance = instance
            if (instance != null) {
                instance.startJob(job)
            } else {
                pendingJobs.add(job)
                context.startService(Intent(context, FileJobService::class.java))
            }
        }

        fun archive(
            sources: List<Path>,
            archiveFile: Path,
            archiveType: String,
            compressorType: String?,
            context: Context
        ): FileJob {
            val fileArchiveJob = ArchiveFileJob(sources, archiveFile, archiveType, compressorType)
            startJob(fileArchiveJob, context)
            return fileArchiveJob
        }

        fun copy(
            sources: List<Path>,
            targetDirectory: Path,
            context: Context,
            clearPaste: () -> Unit
        ): FileJob {
            val fileCopyJob = CopyFileJob(sources, targetDirectory, clearPaste)
            startJob(fileCopyJob, context)
            return fileCopyJob
        }

        fun create(path: Path, createDirectory: Boolean, context: Context) {
            startJob(CreateFileJob(path, createDirectory), context)
        }

        fun delete(paths: List<Path>, context: Context) {
            startJob(DeleteFileJob(paths), context)
        }

        fun move(sources: List<Path>, targetDirectory: Path, context: Context) {
            startJob(MoveFileJob(sources, targetDirectory), context)
        }

        fun installApk(file: Path, context: Context) {
            startJob(InstallApkJob(file), context)
        }

        fun open(file: Path, mimeType: MimeType, withChooser: Boolean, context: Context) {
            startJob(OpenFileJob(file, mimeType, withChooser), context)
        }

        fun rename(path: Path, newName: String, context: Context) {
            startJob(RenameFileJob(path, newName), context)
        }

        fun restoreSeLinuxContext(path: Path, recursive: Boolean, context: Context) {
            startJob(RestoreFileSeLinuxContextJob(path, recursive), context)
        }

        fun setGroup(path: Path, group: PosixGroup, recursive: Boolean, context: Context) {
            startJob(SetFileGroupJob(path, group, recursive), context)
        }

        fun setMode(
            path: Path,
            mode: Set<PosixFileModeBit>,
            recursive: Boolean,
            uppercaseX: Boolean,
            context: Context
        ) {
            startJob(SetFileModeJob(path, mode, recursive, uppercaseX), context)
        }

        fun setOwner(path: Path, owner: PosixUser, recursive: Boolean, context: Context) {
            startJob(SetFileOwnerJob(path, owner, recursive), context)
        }

        fun setSeLinuxContext(
            path: Path,
            seLinuxContext: String,
            recursive: Boolean,
            context: Context
        ) {
            startJob(SetFileSeLinuxContextJob(path, seLinuxContext, recursive), context)
        }

        fun write(
            file: Path,
            content: ByteArray,
            context: Context,
            listener: ((Boolean) -> Unit)?
        ) {
            startJob(WriteFileJob(file, content, listener), context)
        }

        @MainThread
        fun cancelJob(id: Int, operate: (() -> Unit)? = null) {
            pendingJobs.removeFirst { it.id == id }
            instance?.cancelJob(id, operate)
        }

        @MainThread
        fun cancelFirstJob(operate: (() -> Unit)? = null) {
            if (pendingJobs.isNotEmpty()) pendingJobs.removeFirst()
            instance?.cancelFirstJob(operate)
        }
    }
}

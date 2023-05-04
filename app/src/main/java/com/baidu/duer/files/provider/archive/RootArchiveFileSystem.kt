package com.baidu.duer.files.provider.archive

import com.baidu.duer.files.provider.remote.RemoteFileSystemException
import com.baidu.duer.files.provider.root.RootFileService
import com.baidu.duer.files.provider.root.RootFileSystem
import java8.nio.file.FileSystem

internal class RootArchiveFileSystem(
    private val fileSystem: FileSystem
) : RootFileSystem(fileSystem) {
    private var isRefreshNeeded = false

    private val lock = Any()

    fun refresh() {
        synchronized(lock) {
            if (hasRemoteInterface()) {
                isRefreshNeeded = true
            }
        }
    }

    @Throws(RemoteFileSystemException::class)
    fun doRefreshIfNeeded() {
        synchronized(lock) {
            if (isRefreshNeeded) {
                if (hasRemoteInterface()) {
                    RootFileService.refreshArchiveFileSystem(fileSystem)
                }
                isRefreshNeeded = false
            }
        }
    }
}

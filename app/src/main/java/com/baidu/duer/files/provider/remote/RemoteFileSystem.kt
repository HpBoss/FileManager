package com.baidu.duer.files.provider.remote

import java8.nio.file.FileSystem
import java.io.IOException

abstract class RemoteFileSystem(
    private val remoteInterface: RemoteInterface<IRemoteFileSystem>
) : FileSystem() {
    @Throws(IOException::class)
    override fun close() {
        if (!remoteInterface.has()) {
            return
        }
        remoteInterface.get().call { exception -> close(exception) }
    }

    protected fun hasRemoteInterface(): Boolean = remoteInterface.has()

    @Throws(RemoteFileSystemException::class)
    fun ensureRemoteInterface() {
        remoteInterface.get()
    }
}

package com.baidu.duer.files.provider.archive

import com.baidu.duer.files.provider.remote.RemoteFileSystemException
import com.baidu.duer.files.provider.root.RootFileSystemProvider
import java8.nio.file.*
import java.io.IOException
import java.io.InputStream

class RootArchiveFileSystemProvider(scheme: String) : RootFileSystemProvider(scheme) {
    @Throws(IOException::class)
    override fun newInputStream(file: Path, vararg options: OpenOption): InputStream {
        prepareFileSystem(file)
        return super.newInputStream(file, *options)
    }

    @Throws(IOException::class)
    override fun newDirectoryStream(
        directory: Path,
        filter: DirectoryStream.Filter<in Path>
    ): DirectoryStream<Path> {
        prepareFileSystem(directory)
        return super.newDirectoryStream(directory, filter)
    }

    @Throws(IOException::class)
    override fun readSymbolicLink(link: Path): Path {
        prepareFileSystem(link)
        return super.readSymbolicLink(link)
    }

    @Throws(IOException::class)
    override fun checkAccess(path: Path, vararg modes: AccessMode) {
        prepareFileSystem(path)
        super.checkAccess(path, *modes)
    }

    @Throws(RemoteFileSystemException::class)
    private fun prepareFileSystem(path: Path) {
        path as? ArchivePath ?: throw ProviderMismatchException(path.toString())
        val fileSystem = path.fileSystem
        fileSystem.ensureRootInterface()
        fileSystem.doRefreshIfNeededAsRoot()
    }

    @Throws(RemoteFileSystemException::class)
    internal fun doRefreshIfNeeded(path: Path) {
        path as? ArchivePath ?: throw ProviderMismatchException(path.toString())
        path.fileSystem.doRefreshIfNeededAsRoot()
    }
}

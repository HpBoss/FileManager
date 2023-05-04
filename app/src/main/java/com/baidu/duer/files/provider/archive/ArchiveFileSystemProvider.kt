package com.baidu.duer.files.provider.archive

import com.baidu.duer.files.provider.root.RootableFileSystemProvider
import java8.nio.file.Path
import java8.nio.file.attribute.FileAttributeView

object ArchiveFileSystemProvider : RootableFileSystemProvider(
    { LocalArchiveFileSystemProvider(it as ArchiveFileSystemProvider) },
    { RootArchiveFileSystemProvider(LocalArchiveFileSystemProvider.SCHEME) }
) {
    override val localProvider: LocalArchiveFileSystemProvider
        get() = super.localProvider as LocalArchiveFileSystemProvider

    override val rootProvider: RootArchiveFileSystemProvider
        get() = super.rootProvider as RootArchiveFileSystemProvider

    internal fun getOrNewFileSystem(archiveFile: Path): ArchiveFileSystem =
        localProvider.getOrNewFileSystem(archiveFile)

    internal fun removeFileSystem(fileSystem: ArchiveFileSystem) {
        localProvider.removeFileSystem(fileSystem)
    }

    internal fun supportsFileAttributeView(type: Class<out FileAttributeView>): Boolean =
        LocalArchiveFileSystemProvider.supportsFileAttributeView(type)

    internal fun doRefreshIfNeeded(path: Path) {
        rootProvider.doRefreshIfNeeded(path)
    }
}

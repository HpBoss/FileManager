package com.baidu.duer.files.provider.linux

import com.baidu.duer.files.provider.root.RootFileSystemProvider
import com.baidu.duer.files.provider.root.RootableFileSystemProvider
import java8.nio.file.attribute.FileAttributeView

object LinuxFileSystemProvider : RootableFileSystemProvider(
    { LocalLinuxFileSystemProvider(it as LinuxFileSystemProvider) },
    { RootFileSystemProvider(LocalLinuxFileSystemProvider.SCHEME) }
) {
    override val localProvider: LocalLinuxFileSystemProvider
        get() = super.localProvider as LocalLinuxFileSystemProvider

    override val rootProvider: RootFileSystemProvider
        get() = super.rootProvider as RootFileSystemProvider

    internal val fileSystem: LinuxFileSystem
        get() = localProvider.fileSystem

    internal fun supportsFileAttributeView(type: Class<out FileAttributeView>): Boolean =
        LocalLinuxFileSystemProvider.supportsFileAttributeView(type)
}

package com.baidu.duer.files.provider.archive

import com.baidu.duer.files.provider.common.PosixFileAttributeView
import com.baidu.duer.files.provider.common.PosixFileAttributes
import com.baidu.duer.files.provider.root.RootPosixFileAttributeView
import java8.nio.file.Path
import java.io.IOException

internal class RootArchiveFileAttributeView(
    attributeView: PosixFileAttributeView,
    private val path: Path
) : RootPosixFileAttributeView(attributeView) {
    @Throws(IOException::class)
    override fun readAttributes(): PosixFileAttributes {
        ArchiveFileSystemProvider.doRefreshIfNeeded(path)
        return super.readAttributes()
    }
}

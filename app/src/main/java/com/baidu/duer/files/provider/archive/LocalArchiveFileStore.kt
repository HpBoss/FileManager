package com.baidu.duer.files.provider.archive

import com.baidu.duer.files.file.MimeType
import com.baidu.duer.files.file.guessFromPath
import com.baidu.duer.files.provider.common.PosixFileStore
import com.baidu.duer.files.provider.common.size
import java8.nio.file.Path
import java8.nio.file.attribute.FileAttributeView
import org.tukaani.xz.UnsupportedOptionsException
import java.io.IOException

internal class LocalArchiveFileStore(private val archiveFile: Path) : PosixFileStore() {
    override fun refresh() {}

    override fun name(): String = archiveFile.toString()

    override fun type(): String = MimeType.guessFromPath(archiveFile.toString()).value

    override fun isReadOnly(): Boolean = true

    @Throws(IOException::class)
    override fun setReadOnly(readOnly: Boolean) {
        throw UnsupportedOptionsException()
    }

    @Throws(IOException::class)
    override fun getTotalSpace(): Long = archiveFile.size()

    override fun getUsableSpace(): Long = 0

    override fun getUnallocatedSpace(): Long = 0

    override fun supportsFileAttributeView(type: Class<out FileAttributeView>): Boolean =
        ArchiveFileSystemProvider.supportsFileAttributeView(type)

    override fun supportsFileAttributeView(name: String): Boolean =
        name in ArchiveFileAttributeView.SUPPORTED_NAMES
}

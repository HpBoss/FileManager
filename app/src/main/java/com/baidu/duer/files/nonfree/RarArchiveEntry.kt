package com.baidu.duer.files.nonfree

import com.github.junrar.rarfile.FileHeader
import org.apache.commons.compress.archivers.ArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipEncoding
import java.util.*

class RarArchiveEntry(val header: FileHeader, zipEncoding: ZipEncoding) : ArchiveEntry {
    private val name: String

    init {
        @Suppress("DEPRECATION")
        var name = header.fileNameW
        if (name.isNullOrEmpty()) {
            name = zipEncoding.decode(header.fileNameByteArray)
        }
        name = name.replace('\\', '/')
        this.name = name
    }

    override fun getName(): String = name

    override fun getSize(): Long = header.fullUnpackSize

    override fun isDirectory(): Boolean = header.isDirectory

    override fun getLastModifiedDate(): Date = header.mTime
}

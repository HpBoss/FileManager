package com.baidu.duer.files.provider.common

import com.baidu.duer.files.file.MimeType
import com.baidu.duer.files.file.forSpecialPosixFileType
import com.baidu.duer.files.file.guessFromPath
import java8.nio.file.Path
import java8.nio.file.attribute.BasicFileAttributes
import java8.nio.file.spi.FileTypeDetector
import java.io.IOException

object AndroidFileTypeDetector : FileTypeDetector() {
    @Throws(IOException::class)
    override fun probeContentType(path: Path): String {
        val attributes = path.readAttributes(BasicFileAttributes::class.java)
        return getMimeType(path, attributes)
    }

    fun getMimeType(path: Path, attributes: BasicFileAttributes): String {
        MimeType.forSpecialPosixFileType(attributes.posixFileType)?.let { return it.value }
        if (attributes.isDirectory) {
            return MimeType.DIRECTORY.value
        }
        if (attributes is ContentProviderFileAttributes) {
            attributes.mimeType()?.let { return it }
        }
        return MimeType.guessFromPath(path.toString()).value
    }
}

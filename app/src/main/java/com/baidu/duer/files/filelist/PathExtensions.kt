package com.baidu.duer.files.filelist

import com.baidu.duer.files.file.MimeType
import com.baidu.duer.files.file.isSupportedArchive
import com.baidu.duer.files.provider.archive.archiveFile
import com.baidu.duer.files.provider.archive.isArchivePath
import com.baidu.duer.files.provider.linux.isLinuxPath
import java8.nio.file.Path

val Path.name: String
    get() = fileName?.toString() ?: if (isArchivePath) archiveFile.fileName.toString() else "/"

fun Path.toUserFriendlyString(): String = if (isLinuxPath) toFile().path else toUri().toString()

fun Path.isArchiveFile(mimeType: MimeType): Boolean = !isArchivePath && mimeType.isSupportedArchive

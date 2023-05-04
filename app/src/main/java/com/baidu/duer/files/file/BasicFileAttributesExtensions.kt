package com.baidu.duer.files.file

import java8.nio.file.attribute.BasicFileAttributes
import org.threeten.bp.Instant

val BasicFileAttributes.fileSize: FileSize
    get() = size().asFileSize()

val BasicFileAttributes.lastModifiedInstant: Instant
    get() = lastModifiedTime().toInstant()

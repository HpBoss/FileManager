package com.baidu.duer.files.provider.content.resolver

import java8.nio.file.AccessDeniedException
import java8.nio.file.FileSystemException
import java8.nio.file.NoSuchFileException
import java.io.FileNotFoundException

class ResolverException : Exception {
    constructor(message: String?) : super(message)

    constructor(cause: Throwable?) : super(cause)

    fun toFileSystemException(file: String?, other: String? = null): FileSystemException =
        when (cause) {
            is FileNotFoundException -> NoSuchFileException(file, other, message)
            is SecurityException -> AccessDeniedException(file, other, message)
            else -> FileSystemException(file, other, message)
        }.apply { initCause(this@ResolverException) }
}

package com.baidu.duer.files.provider.ftp

import com.baidu.duer.files.provider.ftp.client.NegativeReplyCodeException
import java8.nio.file.FileSystemException
import java.io.IOException

fun IOException.toFileSystemExceptionForFtp(
    file: String?,
    other: String? = null
): FileSystemException =
    when (this) {
        is NegativeReplyCodeException -> toFileSystemException(file, other)
        else ->
            FileSystemException(file, other, message)
                .apply { initCause(this@toFileSystemExceptionForFtp) }
    }

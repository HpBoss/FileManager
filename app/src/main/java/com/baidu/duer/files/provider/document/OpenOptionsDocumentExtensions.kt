package com.baidu.duer.files.provider.document

import com.baidu.duer.files.provider.common.OpenOptions
import java8.nio.file.StandardOpenOption

internal fun OpenOptions.toDocumentMode(): String =
    StringBuilder().apply {
        if (read && write) {
            append("rw")
        } else if (write) {
            append('w')
        } else {
            append('r')
        }
        if (append) {
            append('a')
        }
        if (truncateExisting) {
            append('t')
        }
        if (create || createNew) {
            throw AssertionError(
                "${StandardOpenOption.CREATE} and ${
                    StandardOpenOption.CREATE_NEW
                } should have been handled before calling OpenOptions.toDocumentMode()"
            )
        }
        if (deleteOnClose) {
            throw UnsupportedOperationException(StandardOpenOption.DELETE_ON_CLOSE.toString())
        }
        if (sync) {
            throw UnsupportedOperationException(StandardOpenOption.SYNC.toString())
        }
        if (dsync) {
            throw UnsupportedOperationException(StandardOpenOption.DSYNC.toString())
        }
    }.toString()

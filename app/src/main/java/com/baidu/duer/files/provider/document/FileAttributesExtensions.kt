package com.baidu.duer.files.provider.document

import android.provider.DocumentsContract
import com.baidu.duer.files.util.hasBits
import java8.nio.file.ProviderMismatchException
import java8.nio.file.attribute.BasicFileAttributes

val BasicFileAttributes.documentSupportsThumbnail: Boolean
    get() {
        this as? DocumentFileAttributes ?: throw ProviderMismatchException(toString())
        return flags().hasBits(DocumentsContract.Document.FLAG_SUPPORTS_THUMBNAIL)
    }

package com.baidu.duer.files.util

import android.media.MediaMetadataRetriever
import com.baidu.duer.files.provider.document.isDocumentPath
import com.baidu.duer.files.provider.document.resolver.DocumentResolver
import com.baidu.duer.files.provider.linux.isLinuxPath
import java8.nio.file.Path

val Path.isMediaMetadataRetrieverCompatible: Boolean
    get() = isLinuxPath || isDocumentPath

fun MediaMetadataRetriever.setDataSource(path: Path) {
    when {
        path.isLinuxPath -> setDataSource(path.toFile().path)
        path.isDocumentPath ->
            DocumentResolver.openParcelFileDescriptor(path as DocumentResolver.Path, "r")
                .use { pfd -> setDataSource(pfd.fileDescriptor) }
        else -> throw IllegalArgumentException(path.toString())
    }
}

package com.baidu.duer.files.util

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import com.baidu.duer.files.compat.getPackageArchiveInfoCompat
import com.baidu.duer.files.provider.document.isDocumentPath
import com.baidu.duer.files.provider.document.resolver.DocumentResolver
import com.baidu.duer.files.provider.linux.isLinuxPath
import java8.nio.file.Path
import java.io.Closeable

val Path.isGetPackageArchiveInfoCompatible: Boolean
    get() = isLinuxPath || isDocumentPath

fun PackageManager.getPackageArchiveInfoCompat(
    path: Path,
    flags: Int
): Pair<PackageInfo?, Closeable?> {
    val archiveFilePath: String
    val closeable: Closeable?
    when {
        path.isLinuxPath -> {
            archiveFilePath = path.toFile().path
            closeable = null
        }
        path.isDocumentPath -> {
            val pfd = DocumentResolver.openParcelFileDescriptor(path as DocumentResolver.Path, "r")
            archiveFilePath = "/proc/self/fd/${pfd.fd}"
            closeable = pfd
        }
        else -> throw IllegalArgumentException(path.toString())
    }
    var successful = false
    val packageInfo: PackageInfo?
    try {
        packageInfo = getPackageArchiveInfoCompat(archiveFilePath, flags)?.apply {
            applicationInfo?.apply {
                sourceDir = archiveFilePath
                publicSourceDir = archiveFilePath
            }
        }
        successful = true
    } finally {
        if (!successful) {
            closeable?.close()
        }
    }
    return packageInfo to closeable
}

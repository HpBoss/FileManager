package com.baidu.duer.files.provider.common

import android.os.Parcelable
import java8.nio.file.attribute.FileTime

abstract class AbstractContentProviderFileAttributes : ContentProviderFileAttributes, Parcelable {
    protected abstract val lastModifiedTime: FileTime
    protected abstract val mimeType: String?
    protected abstract val size: Long
    protected abstract val fileKey: Parcelable

    override fun lastModifiedTime(): FileTime = lastModifiedTime

    override fun mimeType(): String? = mimeType

    override fun size(): Long = size

    override fun fileKey(): Parcelable = fileKey
}

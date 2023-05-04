package com.baidu.duer.files.provider.document

import android.net.Uri
import android.os.Parcelable
import com.baidu.duer.files.provider.common.AbstractContentProviderFileAttributes
import com.baidu.duer.files.provider.common.FileTimeParceler
import java8.nio.file.attribute.FileTime
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.WriteWith
import org.threeten.bp.Instant

@Parcelize
internal class DocumentFileAttributes(
    override val lastModifiedTime: @WriteWith<FileTimeParceler> FileTime,
    override val mimeType: String?,
    override val size: Long,
    override val fileKey: Parcelable,
    private val flags: Int
) : AbstractContentProviderFileAttributes() {
    fun flags(): Int = flags

    companion object {
        fun from(
            lastModifiedTimeMillis: Long,
            mimeType: String?,
            size: Long,
            flags: Int,
            uri: Uri
        ): DocumentFileAttributes {
            val lastModifiedTime = FileTime.from(Instant.ofEpochMilli(lastModifiedTimeMillis))
            val fileKey = uri
            return DocumentFileAttributes(lastModifiedTime, mimeType, size, fileKey, flags)
        }
    }
}

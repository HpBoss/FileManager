package com.baidu.duer.files.provider.smb

import android.os.Parcelable
import com.baidu.duer.files.provider.common.AbstractBasicFileAttributes
import com.baidu.duer.files.provider.common.BasicFileType
import com.baidu.duer.files.provider.common.FileTimeParceler
import com.baidu.duer.files.provider.smb.client.ShareInformation
import com.baidu.duer.files.provider.smb.client.ShareType
import java8.nio.file.attribute.FileTime
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.WriteWith
import org.threeten.bp.Instant

@Parcelize
internal class SmbShareFileAttributes(
    override val lastModifiedTime: @WriteWith<FileTimeParceler> FileTime,
    override val lastAccessTime: @WriteWith<FileTimeParceler> FileTime,
    override val creationTime: @WriteWith<FileTimeParceler> FileTime,
    override val type: BasicFileType,
    override val size: Long,
    override val fileKey: Parcelable,
    private val totalSpace: Long?,
    private val usableSpace: Long?,
    private val unallocatedSpace: Long?
) : AbstractBasicFileAttributes() {
    fun totalSpace(): Long? = totalSpace

    fun usableSpace(): Long? = usableSpace

    fun unallocatedSpace(): Long? = unallocatedSpace

    companion object {
        fun from(shareInformation: ShareInformation, path: SmbPath): SmbShareFileAttributes {
            val lastModifiedTime = FileTime.from(Instant.EPOCH)
            val lastAccessTime = lastModifiedTime
            val creationTime = lastModifiedTime
            val type = when (shareInformation.type) {
                ShareType.DISK -> BasicFileType.DIRECTORY
                else -> BasicFileType.OTHER
            }
            val size = 0L
            val fileKey = path
            val shareInfo = shareInformation.shareInfo
            val totalSpace = shareInfo?.totalSpace
            val usableSpace = shareInfo?.callerFreeSpace
            val unallocatedSpace = shareInfo?.freeSpace
            return SmbShareFileAttributes(
                lastModifiedTime, lastAccessTime, creationTime, type, size, fileKey, totalSpace,
                usableSpace, unallocatedSpace
            )
        }
    }
}

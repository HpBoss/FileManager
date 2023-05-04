package com.baidu.duer.files.provider.smb

import android.os.Parcelable
import com.baidu.duer.files.util.hash
import kotlinx.parcelize.Parcelize

@Parcelize
internal class SmbFileKey(
    private val path: SmbPath,
    private val fileId: Long
) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (javaClass != other?.javaClass) {
            return false
        }
        other as SmbFileKey
        return if (fileId != 0L || other.fileId != 0L) {
            path.authority == other.path.authority
                    && path.sharePath!!.name == other.path.sharePath!!.name
                    && fileId == other.fileId
        } else {
            path == other.path
        }
    }

    override fun hashCode(): Int {
        return if (fileId != 0L) {
            hash(path.authority, path.sharePath!!.name, fileId)
        } else {
            path.hashCode()
        }
    }
}

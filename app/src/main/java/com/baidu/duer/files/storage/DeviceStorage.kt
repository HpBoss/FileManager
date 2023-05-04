package com.baidu.duer.files.storage

import android.content.Context
import android.content.Intent
import android.os.storage.StorageVolume
import androidx.annotation.DrawableRes
import com.baidu.duer.files.R
import com.baidu.duer.files.compat.getDescriptionCompat
import com.baidu.duer.files.compat.isPrimaryCompat
import com.baidu.duer.files.compat.pathCompat
import com.baidu.duer.files.util.createIntent
import com.baidu.duer.files.util.putArgs
import com.baidu.duer.files.util.valueCompat
import java8.nio.file.Path
import java8.nio.file.Paths
import kotlinx.parcelize.Parcelize

sealed class DeviceStorage : Storage() {
    override val description: String
        get() = linuxPath

    override val path: Path
        get() = Paths.get(linuxPath)

    abstract override val linuxPath: String

    override fun createEditIntent(): Intent =
        EditDeviceStorageDialogActivity::class.createIntent()
            .putArgs(EditDeviceStorageDialogFragment.Args(this))

    fun copy_(
        customName: String? = this.customName,
        isVisible: Boolean = this.isVisible
    ): DeviceStorage =
        when (this) {
            is FileSystemRoot -> copy(customName, isVisible)
            is PrimaryStorageVolume -> copy(customName, isVisible)
        }
}

@Parcelize
data class FileSystemRoot(
    override val customName: String?,
    override val isVisible: Boolean
) : DeviceStorage() {
    override val id: Long
        get() = "FileSystemRoot".hashCode().toLong()

    override val iconRes: Int
        @DrawableRes
        get() = R.drawable.device_icon_white_24dp

    override fun getDefaultName(context: Context): String =
        context.getString(R.string.storage_file_system_root_title)

    override val linuxPath: String
        get() = LINUX_PATH

    companion object {
        const val LINUX_PATH = "/"
    }
}

@Parcelize
data class PrimaryStorageVolume(
    override val customName: String?,
    override val isVisible: Boolean
) : DeviceStorage() {
    override val id: Long
        get() = "PrimaryStorageVolume".hashCode().toLong()

    override val iconRes: Int
        @DrawableRes
        get() = R.drawable.sd_card_icon_white_24dp

    override fun getDefaultName(context: Context): String =
        storageVolume.getDescriptionCompat(context)

    override val linuxPath: String
        get() = storageVolume.pathCompat

    private val storageVolume: StorageVolume
        get() = StorageVolumeListLiveData.valueCompat.find { it.isPrimaryCompat }!!
}

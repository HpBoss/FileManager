package com.baidu.duer.files.storage

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.DrawableRes
import com.baidu.duer.files.R
import com.baidu.duer.files.compat.getDescriptionCompat
import com.baidu.duer.files.compat.isPrimaryCompat
import com.baidu.duer.files.compat.pathCompat
import com.baidu.duer.files.file.DocumentTreeUri
import com.baidu.duer.files.file.displayName
import com.baidu.duer.files.file.storageVolume
import com.baidu.duer.files.provider.document.createDocumentTreeRootPath
import com.baidu.duer.files.util.createIntent
import com.baidu.duer.files.util.putArgs
import java8.nio.file.Path
import kotlinx.parcelize.Parcelize
import kotlin.random.Random

@Parcelize
data class DocumentTree(
    override val id: Long,
    override val customName: String?,
    val uri: DocumentTreeUri
) : Storage() {
    constructor(
        id: Long?,
        customName: String?,
        uri: DocumentTreeUri
    ) : this(id ?: Random.nextLong(), customName, uri)

    override val iconRes: Int
        @DrawableRes
        // Error: Call requires API level 24 (current min is 21):
        // android.os.storage.StorageVolume#equals [NewApi]
        @SuppressLint("NewApi")
        get() =
            // We are using MANAGE_EXTERNAL_STORAGE to access all storage volumes since R.
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R
                && uri.storageVolume.let { it != null && !it.isPrimaryCompat }
            ) {
                R.drawable.sd_card_icon_white_24dp
            } else {
                super.iconRes
            }

    override fun getDefaultName(context: Context): String =
        uri.storageVolume?.getDescriptionCompat(context) ?: uri.displayName ?: uri.value.toString()

    override val description: String
        get() = uri.value.toString()

    override val path: Path
        get() = uri.value.createDocumentTreeRootPath()

    override val linuxPath: String?
        get() = uri.storageVolume?.pathCompat

    override fun createEditIntent(): Intent =
        EditDocumentTreeDialogActivity::class.createIntent()
            .putArgs(EditDocumentTreeDialogFragment.Args(this))
}

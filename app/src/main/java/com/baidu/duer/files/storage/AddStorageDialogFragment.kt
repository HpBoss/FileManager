package com.baidu.duer.files.storage

import android.app.Dialog
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import com.baidu.duer.files.R
import com.baidu.duer.files.file.asDocumentTreeUri
import com.baidu.duer.files.provider.document.resolver.ExternalStorageProviderHacks
import com.baidu.duer.files.util.createIntent
import com.baidu.duer.files.util.finish
import com.baidu.duer.files.util.putArgs
import com.baidu.duer.files.util.startActivitySafe
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class AddStorageDialogFragment : AppCompatDialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        MaterialAlertDialogBuilder(requireContext(), theme)
            .setTitle(R.string.storage_add_storage_title)
            .apply {
                val items = STORAGE_TYPES.map { getString(it.first) }.toTypedArray<CharSequence>()
                setItems(items) { _, which ->
                    startActivitySafe(STORAGE_TYPES[which].second)
                    finish()
                }
            }
            .create()

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)

        finish()
    }

    companion object {
        private val STORAGE_TYPES = listOfNotNull(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                (R.string.storage_add_storage_android_data
                        to AddDocumentTreeActivity::class.createIntent()
                    .putArgs(
                        AddDocumentTreeFragment.Args(
                            R.string.storage_add_storage_android_data,
                            ExternalStorageProviderHacks.DOCUMENT_URI_ANDROID_DATA
                                .asDocumentTreeUri()
                        )
                    ))
            } else null,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                (R.string.storage_add_storage_android_obb
                        to AddDocumentTreeActivity::class.createIntent()
                    .putArgs(
                        AddDocumentTreeFragment.Args(
                            R.string.storage_add_storage_android_obb,
                            ExternalStorageProviderHacks.DOCUMENT_URI_ANDROID_OBB
                                .asDocumentTreeUri()
                        )
                    ))
            } else null,
            R.string.storage_add_storage_document_tree
                    to AddDocumentTreeActivity::class.createIntent()
                .putArgs(AddDocumentTreeFragment.Args(null, null)),
            R.string.storage_add_storage_ftp_server to EditFtpServerActivity::class.createIntent()
                .putArgs(EditFtpServerFragment.Args()),
            R.string.storage_add_storage_sftp_server to EditSftpServerActivity::class.createIntent()
                .putArgs(EditSftpServerFragment.Args()),
            R.string.storage_add_storage_smb_server to AddLanSmbServerActivity::class.createIntent()
        )
    }
}

package com.baidu.duer.files.storage

import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.baidu.duer.files.file.DocumentTreeUri
import com.baidu.duer.files.file.asDocumentTreeUriOrNull
import com.baidu.duer.files.file.takePersistablePermission
import com.baidu.duer.files.util.ParcelableArgs
import com.baidu.duer.files.util.args
import com.baidu.duer.files.util.finish
import com.baidu.duer.files.util.launchSafe
import kotlinx.parcelize.Parcelize

class AddDocumentTreeFragment : Fragment() {
    private val openDocumentTreeLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree(), this::onOpenDocumentTreeResult
    )

    private val args by args<Args>()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (savedInstanceState == null) {
            openDocumentTreeLauncher.launchSafe(args.treeUri?.value, this)
        }
    }

    private fun onOpenDocumentTreeResult(result: Uri?) {
        val treeUri = result?.asDocumentTreeUriOrNull()
        if (treeUri != null) {
            addDocumentTree(treeUri)
        }
        finish()
    }

    private fun addDocumentTree(treeUri: DocumentTreeUri) {
        treeUri.takePersistablePermission()
        val documentTree = DocumentTree(null, args.customNameRes?.let { getString(it) }, treeUri)
        Storages.addOrReplace(documentTree)
    }

    @Parcelize
    class Args(
        @StringRes val customNameRes: Int?,
        val treeUri: DocumentTreeUri?
    ) : ParcelableArgs
}

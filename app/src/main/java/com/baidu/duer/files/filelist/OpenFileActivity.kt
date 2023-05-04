package com.baidu.duer.files.filelist

import android.content.Intent
import android.os.Bundle
import com.baidu.duer.files.app.AppActivity
import com.baidu.duer.files.app.application
import com.baidu.duer.files.file.MimeType
import com.baidu.duer.files.file.asMimeTypeOrNull
import com.baidu.duer.files.file.fileProviderUri
import com.baidu.duer.files.filejob.FileJobService
import com.baidu.duer.files.provider.archive.isArchivePath
import com.baidu.duer.files.util.createViewIntent
import com.baidu.duer.files.util.extraPath
import com.baidu.duer.files.util.startActivitySafe
import java8.nio.file.Path

class OpenFileActivity : AppActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = intent
        val path = intent.extraPath
        val mimeType = intent.type?.asMimeTypeOrNull()
        if (path != null && mimeType != null) {
            openFile(path, mimeType)
        }
        finish()
    }

    private fun openFile(path: Path, mimeType: MimeType) {
        if (path.isArchivePath) {
            FileJobService.open(path, mimeType, false, this)
        } else {
            val intent = path.fileProviderUri.createViewIntent(mimeType)
                .addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                .apply { extraPath = path }
            startActivitySafe(intent)
        }
    }

    companion object {
        private const val ACTION_OPEN_FILE = "com.baidu.duer.files.intent.action.OPEN_FILE"

        fun createIntent(path: Path, mimeType: MimeType): Intent =
            Intent(ACTION_OPEN_FILE)
                .setPackage(application.packageName)
                .setType(mimeType.value)
                .apply { extraPath = path }
    }
}

package com.baidu.duer.files.filelist

import android.os.Bundle
import com.baidu.duer.files.app.AppActivity
import com.baidu.duer.files.file.MimeType
import com.baidu.duer.files.file.fileProviderUri
import com.baidu.duer.files.util.*
import java8.nio.file.Path
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.WriteWith

// Use a trampoline activity so that we can have a proper icon and title.
class EditFileActivity : AppActivity() {
    private val args by args<Args>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startActivitySafe(args.path.fileProviderUri.createEditIntent(args.mimeType))
        finish()
    }

    @Parcelize
    class Args(
        val path: @WriteWith<ParcelableParceler> Path,
        val mimeType: MimeType
    ) : ParcelableArgs
}

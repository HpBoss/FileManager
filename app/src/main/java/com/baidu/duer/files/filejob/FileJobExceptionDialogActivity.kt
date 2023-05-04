package com.baidu.duer.files.filejob

import android.os.Bundle
import android.view.View
import androidx.fragment.app.commit
import com.baidu.duer.files.app.AppActivity
import com.baidu.duer.files.dialog.FileExceptionDialogFragment
import com.baidu.duer.files.navigation.sendFileExceptionMessage
import com.baidu.duer.files.util.args
import com.baidu.duer.files.util.putArgs

class FileJobExceptionDialogActivity : AppActivity() {
    private val args by args<FileExceptionDialogFragment.Args>()

    private lateinit var fragment: FileExceptionDialogFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Calls ensureSubDecor().
        findViewById<View>(android.R.id.content)
        if (savedInstanceState == null) {
            fragment = FileExceptionDialogFragment().putArgs(args)
            supportFragmentManager.commit {
                add(fragment, FileExceptionDialogFragment::class.java.name)
            }
        } else {
            fragment = supportFragmentManager.findFragmentByTag(
                FileExceptionDialogFragment::class.java.name
            ) as FileExceptionDialogFragment
        }

        sendFileExceptionMessage(args.message)
    }
}

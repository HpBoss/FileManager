package com.baidu.duer.files.filejob

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.commit
import com.baidu.duer.files.app.AppActivity
import com.baidu.duer.files.file.FileItem
import com.baidu.duer.files.util.args
import com.baidu.duer.files.util.putArgs

class FileJobConflictDialogActivity : AppActivity() {
    private val args by args<FileJobConflictDialogFragment.Args>()

    private lateinit var fragment: FileJobConflictDialogFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Calls ensureSubDecor().
        findViewById<View>(android.R.id.content)
        if (savedInstanceState == null) {
            fragment = FileJobConflictDialogFragment().putArgs(args)
            supportFragmentManager.commit {
                add(fragment, FileJobConflictDialogFragment::class.java.name)
            }
        } else {
            fragment = supportFragmentManager.findFragmentByTag(
                FileJobConflictDialogFragment::class.java.name
            ) as FileJobConflictDialogFragment
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if (isFinishing) {
            fragment.onFinish()
        }
    }

    companion object {
        fun getTitle(sourceFile: FileItem, targetFile: FileItem, context: Context): String =
            FileJobConflictDialogFragment.getTitle(sourceFile, targetFile, context)

        fun getMessage(
            sourceFile: FileItem,
            targetFile: FileItem,
            type: CopyMoveType,
            context: Context
        ): String = FileJobConflictDialogFragment.getMessage(sourceFile, targetFile, type, context)
    }
}

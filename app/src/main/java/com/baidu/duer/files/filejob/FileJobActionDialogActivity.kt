package com.baidu.duer.files.filejob

import android.os.Bundle
import android.view.View
import androidx.fragment.app.commit
import com.baidu.duer.files.app.AppActivity
import com.baidu.duer.files.util.args
import com.baidu.duer.files.util.putArgs

class FileJobActionDialogActivity : AppActivity() {
    private val args by args<FileJobActionDialogFragment.Args>()

    private lateinit var fragment: FileJobActionDialogFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Calls ensureSubDecor().
        findViewById<View>(android.R.id.content)
        if (savedInstanceState == null) {
            fragment = FileJobActionDialogFragment().putArgs(args)
            supportFragmentManager.commit {
                add(fragment, FileJobActionDialogFragment::class.java.name)
            }
        } else {
            fragment = supportFragmentManager.findFragmentByTag(
                FileJobActionDialogFragment::class.java.name
            ) as FileJobActionDialogFragment
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if (isFinishing) {
            fragment.onFinish()
        }
    }
}

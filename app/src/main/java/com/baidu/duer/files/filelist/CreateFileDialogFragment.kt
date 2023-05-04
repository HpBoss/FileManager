package com.baidu.duer.files.filelist

import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.baidu.duer.files.R
import com.baidu.duer.files.util.show

class CreateFileDialogFragment : FileNameDialogFragment() {
    override val listener: Listener
        get() = super.listener as Listener

    @StringRes
    override val titleRes: Int = R.string.file_create_file_title

    override fun onOk(name: String) {
        listener.createFile(name)
    }

    companion object {
        fun show(fragment: Fragment) {
            CreateFileDialogFragment().show(fragment)
        }
    }

    interface Listener : FileNameDialogFragment.Listener {
        fun createFile(name: String)
    }
}

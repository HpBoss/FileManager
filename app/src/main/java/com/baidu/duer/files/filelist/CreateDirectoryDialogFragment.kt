package com.baidu.duer.files.filelist

import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.baidu.duer.files.R
import com.baidu.duer.files.util.show

class CreateDirectoryDialogFragment : FileNameDialogFragment() {
    override val listener: Listener
        get() = super.listener as Listener

    @StringRes
    override val titleRes: Int = R.string.file_create_directory_title

    override val permitCheckNameIsValid: Boolean
        get() = false

    override val initialName: String?
        get() = getString(R.string.file_create_directory_title)

    override fun onOk(name: String) {
        listener.createDirectory(name)
        super.dismiss()
    }

    companion object {
        fun show(fragment: Fragment) {
            CreateDirectoryDialogFragment().show(fragment)
        }
    }

    interface Listener : FileNameDialogFragment.Listener {
        fun createDirectory(name: String)
    }
}

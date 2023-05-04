package com.baidu.duer.files.filelist

import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.baidu.duer.files.R
import com.baidu.duer.files.file.FileItem
import com.baidu.duer.files.util.ParcelableArgs
import com.baidu.duer.files.util.args
import com.baidu.duer.files.util.putArgs
import com.baidu.duer.files.util.show
import kotlinx.parcelize.Parcelize

class RenameFileDialogFragment : FileNameDialogFragment() {
    private val args by args<Args>()

    override val listener: Listener
        get() = super.listener as Listener

    @StringRes
    override val titleRes: Int = R.string.file_rename_dialog_title

    override val initialName: String?
        get() = args.file.name

    override fun initData() {
        super.initData()
        binding.nameEdit?.setSelection(0, args.file.baseName.length)
    }

    override fun onOk(name: String) {
        listener.renameFile(args.file, name)
        super.onOk(name)
    }

    companion object {
        fun show(file: FileItem, fragment: Fragment) {
            RenameFileDialogFragment().putArgs(Args(file)).show(fragment)
        }
    }

    @Parcelize
    class Args(val file: FileItem) : ParcelableArgs

    interface Listener : FileNameDialogFragment.Listener {
        fun renameFile(file: FileItem, newName: String)
    }
}

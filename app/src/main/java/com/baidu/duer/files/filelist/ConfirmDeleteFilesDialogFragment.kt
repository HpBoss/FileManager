package com.baidu.duer.files.filelist

import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.baidu.duer.files.R
import com.baidu.duer.files.databinding.CreateDeleteFileDialogBinding
import com.baidu.duer.files.databinding.ItemDialogButtonGroupBinding
import com.baidu.duer.files.databinding.ItemDialogTitleBinding
import com.baidu.duer.files.util.ParcelableArgs
import com.baidu.duer.files.util.args
import com.baidu.duer.files.util.putArgs
import com.baidu.duer.files.util.show
import kotlinx.parcelize.Parcelize

class ConfirmDeleteFilesDialogFragment : FileNameDialogFragment() {
    private val args by args<Args>()

    override val binding: Binding
        get() = super.binding as Binding

    override val listener: Listener
        get() = super.listener as Listener

    @StringRes
    override val titleRes: Int = R.string.delete

    override fun onOk(name: String) {
        listener.deleteFiles(args.files)
        super.onOk(name)
    }

    override fun initData() {
        super.initData()
        binding.deleteTip.text = getString(R.string.file_delete_repeat_confirm, args.files.size)
    }

    override fun onInflateBinding(inflater: LayoutInflater): NameDialogFragment.Binding =
        Binding.inflate(inflater)

    protected class Binding private constructor(
        root: View,
        groupButton: LinearLayout,
        cancelButton: TextView,
        confirmButton: TextView,
        dialogTitle: TextView,
        val deleteTip: TextView
    ) : NameDialogFragment.Binding(
        root,
        groupButton,
        cancelButton,
        confirmButton,
        dialogTitle
    ) {
        companion object {
            fun inflate(inflater: LayoutInflater): Binding {
                val binding = CreateDeleteFileDialogBinding.inflate(inflater)
                val bindingRoot = binding.root
                val itemDialogButtonGroupBinding = ItemDialogButtonGroupBinding.bind(bindingRoot)
                val itemDialogTitleBinding = ItemDialogTitleBinding.bind(bindingRoot)
                return Binding(
                    bindingRoot,
                    itemDialogButtonGroupBinding.buttonGroup,
                    itemDialogButtonGroupBinding.buttonCancel,
                    itemDialogButtonGroupBinding.buttonConfirm,
                    itemDialogTitleBinding.dialogTitle,
                    binding.deleteTip
                )
            }
        }
    }

    companion object {
        fun show(files: FileItemSet, fragment: Fragment) {
            ConfirmDeleteFilesDialogFragment().putArgs(Args(files)).show(fragment)
        }
    }

    @Parcelize
    class Args(val files: FileItemSet) : ParcelableArgs

    interface Listener : FileNameDialogFragment.Listener {
        fun deleteFiles(files: FileItemSet)
    }
}

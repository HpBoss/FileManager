package com.baidu.duer.files.dialog

import android.os.Bundle
import android.os.Parcel
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.StringRes
import com.baidu.duer.files.R
import com.baidu.duer.files.databinding.CreateArchiveProgressDialogBinding
import com.baidu.duer.files.databinding.ItemDialogButtonGroupBinding
import com.baidu.duer.files.databinding.ItemDialogTitleBinding
import com.baidu.duer.files.filejob.FileJobAction
import com.baidu.duer.files.filelist.FileNameDialogFragment
import com.baidu.duer.files.filelist.NameDialogFragment
import com.baidu.duer.files.util.*
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.WriteWith

/**
 * @Author : 何飘
 * @CreateTime : 2023/4/2
 * @Description :
 */
class FileExceptionDialogFragment : FileNameDialogFragment() {
    private val args by args<Args>()

    override val binding: Binding
        get() = super.binding as Binding

    override fun onTouchOutsideIsCancel(): Boolean = false

    override fun initData() {
        super.initData()
        binding.cancelButton.visibility = View.GONE
        binding.confirmButton.text = getString(R.string.cancel_button_text_know)
        binding.progressContent.text = args.message
    }

    @StringRes
    override val titleRes: Int = R.string.file_unzip_exception_title

    override fun onInflateBinding(inflater: LayoutInflater): NameDialogFragment.Binding =
        Binding.inflate(inflater)

    override fun onOk(name: String) {
        Log.i(TAG, "onOk")
        args.listener(FileJobAction.CANCELED)
        dismiss()
        finish()
    }

    companion object {
        private const val TAG = "FileExceptionDialogFragment"
    }

    @Parcelize
    class Args(
        val title: String,
        val message: String,
        val listener: @WriteWith<ListenerParceler>() (FileJobAction) -> Unit
    ) : ParcelableArgs {
        object ListenerParceler : Parceler<(FileJobAction) -> Unit> {
            override fun create(parcel: Parcel): (FileJobAction) -> Unit =
                parcel.readParcelable<RemoteCallback>()!!.let {
                    { action ->
                        it.sendResult(Bundle().putArgs(ListenerArgs(action)))
                    }
                }

            override fun ((FileJobAction) -> Unit).write(parcel: Parcel, flags: Int) {
                parcel.writeParcelable(RemoteCallback {
                    val args = it.getArgs<ListenerArgs>()
                    this(args.action)
                }, flags)
            }

            @Parcelize
            private class ListenerArgs(
                val action: FileJobAction,
            ) : ParcelableArgs
        }
    }

    protected class Binding private constructor(
        root: View,
        groupButton: LinearLayout,
        cancelButton: TextView,
        confirmButton: TextView,
        dialogTitle: TextView,
        val progressContent: TextView
    ) : NameDialogFragment.Binding(
        root,
        groupButton,
        cancelButton,
        confirmButton,
        dialogTitle
    ) {
        companion object {
            fun inflate(inflater: LayoutInflater): Binding {
                val binding = CreateArchiveProgressDialogBinding.inflate(inflater)
                val bindingRoot = binding.root
                val itemDialogButtonGroupBinding = ItemDialogButtonGroupBinding.bind(bindingRoot)
                val itemDialogTitleBinding = ItemDialogTitleBinding.bind(bindingRoot)
                return Binding(
                    bindingRoot,
                    itemDialogButtonGroupBinding.buttonGroup,
                    itemDialogButtonGroupBinding.buttonCancel,
                    itemDialogButtonGroupBinding.buttonConfirm,
                    itemDialogTitleBinding.dialogTitle,
                    binding.progressContent
                )
            }
        }
    }
}

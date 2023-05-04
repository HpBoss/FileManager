package com.baidu.duer.files.dialog

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.baidu.duer.files.R
import com.baidu.duer.files.databinding.CreateArchiveProgressDialogBinding
import com.baidu.duer.files.databinding.ItemDialogButtonGroupBinding
import com.baidu.duer.files.databinding.ItemDialogTitleBinding
import com.baidu.duer.files.file.asFileSize
import com.baidu.duer.files.filelist.FileNameDialogFragment
import com.baidu.duer.files.filelist.NameDialogFragment
import com.baidu.duer.files.navigation.receiveFileOperateProgress
import com.baidu.duer.files.util.ParcelableArgs
import com.baidu.duer.files.util.args
import com.baidu.duer.files.util.putArgs
import com.baidu.duer.files.util.show
import java8.nio.file.Path
import java8.nio.file.Paths
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * @Author : 何飘
 * @CreateTime : 2023/3/3
 * @Description :
 */
class CreateArchiveProgressDialogFragment : FileNameDialogFragment() {
    private val args by args<Args>()
    private var archiving = true

    override val binding: Binding
        get() = super.binding as Binding

    override val listener: Listener
        get() = super.listener as Listener

    // 压缩过程，除非点击取消不然Dialog不消失
    override fun onTouchOutsideIsCancel(): Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        if (savedInstanceState != null && !savedInstanceState.getBoolean(ARCHIVE_STATE)) {
            showCompressComplete()
        }
        return view
    }

    override fun initData() {
        super.initData()
        binding.confirmButton.visibility = View.GONE
        // LiveEventBus替代EventBus进行消息发送、接收
        receiveFileOperateProgress(this) {
            binding.progressContent.text =
                getString(
                    R.string.file_compress_progress,
                    it.progress.asFileSize().formatHumanShortReadable(requireContext()),
                    it.total.asFileSize().formatHumanShortReadable(requireContext())
                )
            val progress = if (it.total != 0L) {
                BigDecimal((it.progress.toFloat() / it.total).toDouble()).setScale(
                    2,
                    RoundingMode.HALF_UP
                ).toFloat() * 100
            } else 0
            binding.dialogTitle.text = getString(R.string.file_compressing, progress.toInt())
            if (it.progress == it.total && it.progress > 0) showCompressComplete()
        }
        baseDialog.setOnKeyListener { _, keyCode, _ ->
            Log.i(TAG, "onKeyListener, keyCode：$keyCode")
            if (keyCode == KeyEvent.KEYCODE_BACK && archiving) {
                listener.cancelArchiveFile(Paths.get(args.pathName), args.name)
            }
            false
        }
    }

    @StringRes
    override val titleRes: Int = R.string.file_compressing

    override fun onInflateBinding(inflater: LayoutInflater): NameDialogFragment.Binding =
        Binding.inflate(inflater)

    override fun onCancel() {
        super.onCancel()
        if (archiving) listener.cancelArchiveFile(Paths.get(args.pathName), args.name)
    }

    override fun onOk(name: String) {
        listener.navigateToFilePath(Paths.get(args.pathName))
        super.onOk(name)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(ARCHIVE_STATE, archiving)
    }

    private fun showCompressComplete() {
        archiving = false
        binding.dialogTitle.text = getString(R.string.file_compress_complete)
        binding.progressContent.text = getString(R.string.file_compress_to_location, args.pathName)
        binding.cancelButton.visibility = View.VISIBLE
        binding.confirmButton.visibility = View.VISIBLE
        binding.cancelButton.text = getString(R.string.file_compress_complete_button_text)
        binding.confirmButton.text = getString(R.string.confirm_button_text_check)
        baseDialog.setCanceledOnTouchOutside(true)
    }

    companion object {
        private const val TAG = "ArchiveProgressDialogFragment"
        private const val ARCHIVE_STATE = "archive_state"
        fun show(pathName: String, name: String, fragment: Fragment) {
            CreateArchiveProgressDialogFragment().putArgs(Args(pathName, name)).show(fragment)
        }
    }

    @Parcelize
    class Args(val pathName: String, val name: String) : ParcelableArgs

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

    interface Listener : FileNameDialogFragment.Listener {
        fun cancelArchiveFile(path: Path?, name: String)
        fun navigateToFilePath(path: Path)
    }
}

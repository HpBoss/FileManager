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
import com.baidu.duer.files.filejob.FileJobService
import com.baidu.duer.files.filelist.NameDialogFragment
import com.baidu.duer.files.message.FileOperateTaskProgress
import com.baidu.duer.files.message.OperateType
import com.baidu.duer.files.message.TaskProgressInfo
import com.baidu.duer.files.navigation.receiveFileOperateProgress
import com.baidu.duer.files.navigation.sendFileOperateProgress
import com.baidu.duer.files.util.ParcelableArgs
import com.baidu.duer.files.util.args
import com.baidu.duer.files.util.putArgs
import com.baidu.duer.files.util.show
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * @Author : 何飘
 * @CreateTime : 2023/4/20
 * @Description :
 */
class CopyMoveDeleteProgressDialogFragment : NameDialogFragment() {
    private val args by args<Args>()
    private var progressing = true
    private var mProgress: Int = 0

    override val binding: Binding
        get() = super.binding as Binding

    override fun onTouchOutsideIsCancel(): Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        // 保存Progress的目的，可能在删除大量文件时，某些文件删除较慢，停留在一个进度值较长时间,
        // 此时DialogFragment界面进行了重建，需要展示旋转之前的进度值
        mProgress = savedInstanceState?.getInt(PROGRESS_VALUE) ?: mProgress
        return view
    }

    override fun initData() {
        super.initData()
        binding.confirmButton.visibility = View.GONE
        // LiveEventBus替代EventBus进行消息发送、接收
        binding.dialogTitle.text = getString(
            args.type.getResourceId(
                R.string.file_copy_progress_title,
                R.string.file_move_progress_title,
                R.string.file_delete_progress_title,
            )
        )
        binding.progressContent.text =
            getString(
                args.type.getResourceId(
                    R.string.file_copy_progress_content,
                    R.string.file_move_progress_content,
                    R.string.file_delete_progress_content
                ), mProgress
            )
        receiveFileOperateProgress(this) {
            mProgress = if (it.total != 0L) {
                (BigDecimal((it.progress.toFloat() / it.total).toDouble()).setScale(
                    2,
                    RoundingMode.HALF_UP
                ).toFloat() * 100).toInt()
            } else 0
            binding.progressContent.text =
                getString(
                    args.type.getResourceId(
                        R.string.file_copy_progress_content,
                        R.string.file_move_progress_content,
                        R.string.file_delete_progress_content
                    ), mProgress
                )
            if (it.progress == it.total && it.progress > 0) dismiss()
        }
        baseDialog.setOnKeyListener { _, keyCode, _ ->
            Log.i(TAG, "onKeyListener, keyCode：$keyCode")
            if (keyCode == KeyEvent.KEYCODE_BACK && progressing) {
                cancelFileOperate()
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
        if (progressing) cancelFileOperate()
    }

    private fun cancelFileOperate() {
        FileJobService.cancelFirstJob {
            sendFileOperateProgress(
                TaskProgressInfo(
                    0L,
                    0L,
                    FileOperateTaskProgress.FINISH,
                    when (args.type) {
                        CopyMoveDeleteType.COPY -> OperateType.CANCEL_COPY
                        CopyMoveDeleteType.MOVE -> OperateType.CANCEL_MOVE
                        CopyMoveDeleteType.DELETE -> OperateType.CANCEL_DELETE
                    }
                )
            )
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(PROGRESS_VALUE, mProgress)
    }

    companion object {
        private const val TAG = "ArchiveProgressDialogFragment"
        private const val PROGRESS_VALUE = "progress_value"
        fun show(type: CopyMoveDeleteType, fragment: Fragment) {
            CopyMoveDeleteProgressDialogFragment().putArgs(Args(type)).show(fragment)
        }
    }

    @Parcelize
    class Args(val type: CopyMoveDeleteType) : ParcelableArgs

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

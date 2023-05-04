package com.baidu.duer.files.dialog

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.baidu.duer.files.R
import com.baidu.duer.files.databinding.ItemDialogButtonGroupBinding
import com.baidu.duer.files.databinding.ItemDialogTitleBinding
import com.baidu.duer.files.databinding.UnzipFileDialogBinding
import com.baidu.duer.files.dialog.UnzipFileDialogFragment.Binding.Companion.NONE
import com.baidu.duer.files.dialog.UnzipFileDialogFragment.Binding.Companion.UNZIPPING
import com.baidu.duer.files.dialog.UnzipFileDialogFragment.Binding.Companion.UNZIP_COMPLETE
import com.baidu.duer.files.file.FileItem
import com.baidu.duer.files.file.asFileSize
import com.baidu.duer.files.filelist.FileNameDialogFragment
import com.baidu.duer.files.filelist.NameDialogFragment
import com.baidu.duer.files.message.FileOperateTaskProgress
import com.baidu.duer.files.message.TargetFileName
import com.baidu.duer.files.navigation.receiveFileExceptionMessage
import com.baidu.duer.files.navigation.receiveFileOperateProgress
import com.baidu.duer.files.selectfile.SelectFileActivity
import com.baidu.duer.files.util.*
import com.jeremyliao.liveeventbus.LiveEventBus
import java8.nio.file.Path
import java8.nio.file.Paths
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * @Author : 何飘
 * @CreateTime : 2023/3/9
 * @Description :
 */
class UnzipFileDialogFragment : FileNameDialogFragment() {
    private val args by args<Args>()
    private var unzipBaseName = ""
    private var unzipStatus = NONE
    private var mProgress: Int = 0
    private lateinit var targetPath: Path
    private lateinit var archiveActivityLauncher: ActivityResultLauncher<Intent>

    override val binding: Binding
        get() = super.binding as Binding

    override val listener: Listener
        get() = super.listener as Listener

    @StringRes
    override val titleRes: Int = R.string.file_unzip_file

    override fun onCreate(savedInstanceState: Bundle?) {
        archiveActivityLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == RESULT_CODE) {
                val path = it.data?.getStringExtra(PATH)
                targetPath = Paths.get(path)
                // 如果从tabName中映射出的name为空，则直接展示完整path
                binding.currentStorageLocation.text =
                    getString(R.string.storage_location, path)
            }
        }
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        unzipStatus = savedInstanceState?.getInt(UNZIP_STATE) ?: NONE
        unzipBaseName = savedInstanceState?.getString(UNZIP_BASE_NAME) ?: ""
        mProgress = savedInstanceState?.getInt(UNZIP_PROGRESS) ?: mProgress
        if (unzipStatus == UNZIP_COMPLETE) {
            showUnzipComplete()
        } else if (unzipStatus == UNZIPPING) {
            startUnzip()
        }
        return view
    }

    override fun initData() {
        super.initData()
        targetPath = Paths.get(args.defaultPathName)
        binding.currentStorageLocation.text =
            getString(R.string.storage_location, args.defaultPathName)
        binding.modifyButton.setOnClickListener {
            archiveActivityLauncher.launch(
                Intent(
                    SelectFileActivity::class.createIntent()
                        .putExtra(SELECT_TYPE, ARCHIVES).putExtra(
                            TITLE, getString(
                                R.string.file_unzip_file
                            )
                        )
                )
            )
        }
        baseDialog.setOnKeyListener { _, keyCode, _ ->
            Log.i(TAG, "onKeyListener, keyCode：$keyCode")
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                listener.cancelUnzipFile(targetPath, unzipBaseName)
            }
            false
        }
    }

    override fun onInflateBinding(inflater: LayoutInflater): NameDialogFragment.Binding =
        Binding.inflate(inflater)

    override fun onOk(name: String) {
        when (unzipStatus) {
            NONE -> {
                // 确定，开始解压
                startUnzip()
            }
            UNZIP_COMPLETE -> {
                // 查看
                listener.navigateToFilePath(
                    targetPath.resolve(unzipBaseName)
                )
                super.onOk(name)
            }
        }
    }

    override fun onCancel() {
        if (unzipStatus != UNZIP_COMPLETE) {
            listener.cancelUnzipFile(targetPath, unzipBaseName)
        }
        super.onCancel()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(UNZIP_STATE, unzipStatus)
        outState.putString(UNZIP_BASE_NAME, unzipBaseName)
        outState.putInt(UNZIP_PROGRESS, mProgress)
    }

    private fun showUnzipComplete() {
        baseDialog.setCanceledOnTouchOutside(false)
        binding.apply {
            dialogTitle.text = getString(R.string.file_unzip_complete)
            unzipProgress.text = getString(R.string.file_unzip_to_location, targetPath.toString())
            cancelButton.text = getString(R.string.cancel_button_text_know)
            confirmButton.text = getString(R.string.confirm_button_text_check)
            confirmButton.visibility = View.VISIBLE
        }
    }

    private fun startUnzip() {
        baseDialog.setCanceledOnTouchOutside(false)
        LiveEventBus
            .get(TargetFileName::class.java)
            .observe(
                this
            ) {
                unzipBaseName = it.fileName
            }
        // 当文件解压发生异常时，需要清除掉当前的Dialog
        receiveFileExceptionMessage(this) {
            Log.i(TAG, "exception: $it")
            onCancel()
        }
        binding.apply {
            dialogTitle.text = getString(R.string.file_unziping, 0)
            locationLayout.visibility = View.GONE
            unzipProgress.visibility = View.VISIBLE
            confirmButton.visibility = View.GONE
            unzipProgress.text = getString(R.string.file_unzip_progress, "0 B", "0 MB")
            unzipStatus = UNZIPPING
            listener.unzipFile(targetPath)
            receiveFileOperateProgress(this@UnzipFileDialogFragment) {
                if (unzipStatus == UNZIP_COMPLETE) return@receiveFileOperateProgress
                // 界面元素的改变，不能单纯依靠点击事件触发，接收到事件进度数据才是关键，因此把view的切换提取到receive中
                unzipProgress.text = getString(
                    R.string.file_unzip_progress,
                    it.progress.asFileSize().formatHumanShortReadable(requireContext()),
                    it.total.asFileSize().formatHumanShortReadable(requireContext())
                )
                // 先保留两位小数，再转换为整数
                mProgress = if (it.total != 0L) {
                    (BigDecimal((it.progress.toFloat() / it.total).toDouble()).setScale(
                        2,
                        RoundingMode.HALF_UP
                    ).toFloat() * 100).toInt()
                } else 0
                dialogTitle.text = getString(R.string.file_unziping, mProgress)
                if (it.progressType == FileOperateTaskProgress.FINISH || mProgress == 100) {
                    unzipStatus = UNZIP_COMPLETE
                    showUnzipComplete()
                }
            }
        }
    }

    companion object {
        private const val TAG = "UnzipFileDialogFragment"
        private const val UNZIP_STATE = "unzip_state"
        private const val UNZIP_BASE_NAME = "unzip_base_name"
        private const val UNZIP_PROGRESS = "unzip_progress"
        fun show(file: FileItem, defaultPathName: String, fragment: Fragment) {
            UnzipFileDialogFragment().putArgs(Args(file, defaultPathName)).show(fragment)
        }
    }

    @Parcelize
    class Args(val file: FileItem, val defaultPathName: String) : ParcelableArgs

    protected class Binding private constructor(
        root: View,
        groupButton: LinearLayout,
        cancelButton: TextView,
        confirmButton: TextView,
        dialogTitle: TextView,
        val currentStorageLocation: TextView,
        val modifyButton: TextView,
        val unzipProgress: TextView,
        val locationLayout: LinearLayout
    ) : NameDialogFragment.Binding(
        root,
        groupButton,
        cancelButton,
        confirmButton,
        dialogTitle
    ) {
        companion object {
            const val NONE = 0
            const val UNZIPPING = 1
            const val UNZIP_COMPLETE = 2

            fun inflate(inflater: LayoutInflater): Binding {
                val binding = UnzipFileDialogBinding.inflate(inflater)
                val bindingRoot = binding.root
                val itemDialogButtonGroupBinding = ItemDialogButtonGroupBinding.bind(bindingRoot)
                val itemDialogTitleBinding = ItemDialogTitleBinding.bind(bindingRoot)
                return Binding(
                    bindingRoot,
                    itemDialogButtonGroupBinding.buttonGroup,
                    itemDialogButtonGroupBinding.buttonCancel,
                    itemDialogButtonGroupBinding.buttonConfirm,
                    itemDialogTitleBinding.dialogTitle,
                    binding.idCurrentStorageLocation,
                    binding.idModifyButton,
                    binding.unzipProgress,
                    binding.locationLayout
                )
            }
        }
    }

    interface Listener : FileNameDialogFragment.Listener {
        fun unzipFile(path: Path)
        fun cancelUnzipFile(path: Path, name: String)
        fun navigateToFilePath(path: Path)
    }
}

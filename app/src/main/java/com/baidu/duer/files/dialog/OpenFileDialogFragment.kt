package com.baidu.duer.files.dialog

import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.baidu.duer.files.R
import com.baidu.duer.files.database.AppDatabase
import com.baidu.duer.files.database.Mime
import com.baidu.duer.files.databinding.ItemDialogButtonGroupBinding
import com.baidu.duer.files.databinding.ItemDialogTitleBinding
import com.baidu.duer.files.databinding.OpenFileDialogBinding
import com.baidu.duer.files.file.FileItem
import com.baidu.duer.files.file.fileProviderUri
import com.baidu.duer.files.file.intentType
import com.baidu.duer.files.filelist.FileNameDialogFragment
import com.baidu.duer.files.filelist.NameDialogFragment
import com.baidu.duer.files.navigation.getOpenIntentActivities
import com.baidu.duer.files.util.*
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.parcelize.Parcelize


/**
 * @Author : 何飘
 * @CreateTime : 2023/3/8
 * @Description :
 */
class OpenFileDialogFragment : FileNameDialogFragment(), ClickListener {
    private val args by args<Args>()
    private var selectIndex = 0
    private lateinit var adapter: OpenFileDialogAdapter
    private lateinit var resolveInfoCheckList: List<ResolveInfoCheck>
    private val resolveInfoList by lazy {
        getOpenIntentActivities(args.file.path.fileProviderUri).toMutableList()
    }

    override val binding: Binding
        get() = super.binding as Binding

    override val listener: Listener
        get() = super.listener as Listener

    @StringRes
    override val titleRes: Int = R.string.file_select_appliction_open

    override fun initData() {
        super.initData()
        binding.applicationRecycleView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        resolveInfoCheckList = resolveInfoList.map { ResolveInfoCheck(it) }
        // 默认选择第一个
        resolveInfoCheckList.getOrNull(0)?.isChecked = true
        adapter = OpenFileDialogAdapter(this)
        binding.applicationRecycleView.adapter = adapter
        adapter.replace(resolveInfoCheckList)
        binding.cancelButton.text = getString(R.string.file_only_one_with_open)
        binding.confirmButton.text = getString(R.string.file_all_along_with_open)
    }

    override fun onInflateBinding(inflater: LayoutInflater): NameDialogFragment.Binding =
        Binding.inflate(inflater)

    override fun onOk(name: String) {
        // 始终
        resolveInfoList.getOrNull(selectIndex)?.let {
            // 数据插入始终打开某类型文件的Application信息
            lifecycleScope.launch {
                try {
                    withTimeout(3000) {
                        AppDatabase.getDatabase(requireContext())?.mimeDao()?.insert(
                            Mime(
                                mimeType = args.file.mimeType.value,
                                packageName = it.activityInfo.packageName,
                                className = it.activityInfo.name
                            )
                        )
                    }
                } catch (e: TimeoutCancellationException) {
                    e.printStackTrace()
                }
            }
            startActivity(
                args.file.path.fileProviderUri.createOpenFileOtherApk(
                    args.file.mimeType.intentType,
                    it.activityInfo.packageName, it.activityInfo.name
                ).apply {
                    extraPath = args.file.path
                }
            )
        }
        super.onOk(name)
    }

    override fun onCancel() {
        // 仅此一次
        resolveInfoList.getOrNull(selectIndex)?.let {
            startActivity(
                args.file.path.fileProviderUri.createOpenFileOtherApk(
                    args.file.mimeType.intentType,
                    it.activityInfo.packageName, it.activityInfo.name
                ).apply {
                    extraPath = args.file.path
                }
            )
        }
        super.onCancel()
    }

    override fun onClick(index: Int) {
        resolveInfoCheckList.getOrNull(selectIndex)?.isChecked = false
        resolveInfoCheckList.getOrNull(index)?.isChecked = true
        selectIndex = index
        adapter.replace(resolveInfoCheckList)
    }

    companion object {
        fun show(file: FileItem, fragment: Fragment) {
            OpenFileDialogFragment().putArgs(Args(file)).show(fragment)
        }
    }

    @Parcelize
    class Args(val file: FileItem) : ParcelableArgs

    protected class Binding private constructor(
        root: View,
        groupButton: LinearLayout,
        cancelButton: TextView,
        confirmButton: TextView,
        dialogTitle: TextView,
        val applicationRecycleView: RecyclerView
    ) : NameDialogFragment.Binding(
        root,
        groupButton,
        cancelButton,
        confirmButton,
        dialogTitle
    ) {
        companion object {
            fun inflate(inflater: LayoutInflater): Binding {
                val binding = OpenFileDialogBinding.inflate(inflater)
                val bindingRoot = binding.root
                val itemDialogButtonGroupBinding = ItemDialogButtonGroupBinding.bind(bindingRoot)
                val itemDialogTitleBinding = ItemDialogTitleBinding.bind(bindingRoot)
                return Binding(
                    bindingRoot,
                    itemDialogButtonGroupBinding.buttonGroup,
                    itemDialogButtonGroupBinding.buttonCancel,
                    itemDialogButtonGroupBinding.buttonConfirm,
                    itemDialogTitleBinding.dialogTitle,
                    binding.applicationRecycleView
                )
            }
        }
    }
}

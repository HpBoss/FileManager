package com.baidu.duer.files.dialog

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.baidu.duer.files.R
import com.baidu.duer.files.databinding.CreateArchiveDialogBinding
import com.baidu.duer.files.databinding.ItemDialogButtonGroupBinding
import com.baidu.duer.files.databinding.ItemDialogTitleBinding
import com.baidu.duer.files.databinding.NameDialogNameIncludeBinding
import com.baidu.duer.files.filelist.*
import com.baidu.duer.files.navigation.querySameFileName
import com.baidu.duer.files.selectfile.SelectFileActivity
import com.baidu.duer.files.util.*
import com.google.android.material.textfield.TextInputLayout
import java8.nio.file.Path
import java8.nio.file.Paths
import kotlinx.parcelize.Parcelize
import org.apache.commons.compress.archivers.ArchiveStreamFactory
import org.apache.commons.compress.compressors.CompressorStreamFactory

class CreateArchiveDialogFragment : FileNameDialogFragment(), ClickListener {
    private val args by args<Args>()
    private lateinit var targetPath: Path
    private var selectIndex = 0
    private lateinit var adapter: CompressWayListAdapter
    private val compressWayList by lazy {
        arrayListOf(
            CompressWayItem(getString(R.string.file_create_archive_type_zip), true),
            CompressWayItem(getString(R.string.file_create_archive_type_tar_xz), false),
            CompressWayItem(getString(R.string.file_create_archive_type_7z), false)
        )
    }
    private lateinit var archiveActivityLauncher: ActivityResultLauncher<Intent>

    override val binding: Binding
        get() = super.binding as Binding

    override val listener: Listener
        get() = super.listener as Listener

    @StringRes
    override val titleRes: Int = R.string.file_create_archive_title

    override val isArchiveFile: Boolean
        get() = true

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
                binding.nameLayout?.apply {
                    error = null
                    isErrorEnabled = false
                }
            }
        }
        super.onCreate(savedInstanceState)
    }

    override fun initData() {
        super.initData()
        targetPath = Paths.get(args.defaultPathName)
        val files = args.files
        var name: String? = null
        if (files.size == 1) {
            name = files.single().path.fileName.toString()
        } else {
            // 当多个文件或文件夹被压缩时，压缩包名称默认为"归档"、"归档 (2)"、"归档 (3)"。。。
            val parent = files.mapTo(mutableSetOf()) { it.path.parent }.singleOrNull()
            if (parent != null && parent.nameCount > 0) {
                val prefix = getString(R.string.file_type_name_archive_num)
                val suffix = if (args.repeatNum == 0) "" else getString(
                    R.string.repeat_title,
                    args.repeatNum
                )
                name = prefix + suffix
            }
        }
        binding.currentStorageLocation.text =
            getString(R.string.storage_location, args.defaultPathName)
        binding.modifyButton.setOnClickListener {
            archiveActivityLauncher.launch(
                Intent(
                    SelectFileActivity::class.createIntent()
                        .putExtra(SELECT_TYPE, ARCHIVES).putExtra(
                            TITLE, getString(
                                R.string.select_archive_file_to_path_count,
                                files.size
                            )
                        ).putExtra(PATH_DATA, args.files.map { it.path.toString() }.toTypedArray())
                )
            )
        }
        binding.compressRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        adapter = CompressWayListAdapter(this, requireContext())
        binding.compressRecyclerView.adapter = adapter
        adapter.replace(compressWayList)
        name?.let { binding.nameEdit?.setTextWithSelection(it) }
    }

    override fun isCurrentPath(): Boolean = targetPath.toString() == args.defaultPathName

    override fun onInflateBinding(inflater: LayoutInflater): NameDialogFragment.Binding =
        Binding.inflate(inflater)

    override val name: String
        get() {
            val extension = compressWayList.getOrNull(selectIndex)?.compressName
            return "${super.name}$extension"
        }

    override fun onOk(name: String) {
        Log.i(Binding.TAG, "targetPath: $targetPath, name: $name")
        val archiveType: String
        val compressorType: String?
        when (selectIndex) {
            0 -> {
                archiveType = ArchiveStreamFactory.ZIP
                compressorType = null
            }
            1 -> {
                archiveType = ArchiveStreamFactory.TAR
                compressorType = CompressorStreamFactory.XZ
            }
            2 -> {
                archiveType = ArchiveStreamFactory.SEVEN_Z
                compressorType = null
            }
            else -> {
                archiveType = ""
                compressorType = null
            }
        }
        // 如果当前压缩路径和文件原路径不一致，那么需要立即进行比较，查询结果是耗时的需要执行结束后再返回结果给Fragment；
        // 当压缩目的路径就是当前文件路径时，在FileNameDialogFragment中就会进行比较，这里直接返回结果就行
        if (!isCurrentPath()) {
            querySameFileName(viewLifecycleOwner, targetPath) {
                if (it is Success && it.value.any { file ->
                        file.name == name && file.isArchiveFile && file.extension == archiveType
                    }) {
                    binding.nameLayout?.error =
                        getString(R.string.file_name_error_already_exists)
                    return@querySameFileName
                }
                notifyArchive(name, archiveType, compressorType)
            }
        } else {
            notifyArchive(name, archiveType, compressorType)
        }
    }

    private fun notifyArchive(name: String, archiveType: String, compressorType: String?) {
        listener.archive(
            targetPath,
            args.files,
            name,
            archiveType,
            compressorType
        )
        super.onOk(name)
    }

    override fun onClick(index: Int) {
        compressWayList.getOrNull(selectIndex)?.isChecked = false
        compressWayList.getOrNull(index)?.isChecked = true
        selectIndex = index
        adapter.replace(compressWayList)
    }

    companion object {
        fun show(files: FileItemSet, repeatNum: Int, defaultPathName: String, fragment: Fragment) {
            CreateArchiveDialogFragment().putArgs(Args(files, repeatNum, defaultPathName))
                .show(fragment)
        }
    }

    @Parcelize
    class Args(val files: FileItemSet, val repeatNum: Int, val defaultPathName: String) :
        ParcelableArgs

    protected class Binding private constructor(
        root: View,
        nameLayout: TextInputLayout,
        nameEdit: EditText,
        groupButton: LinearLayout,
        cancelButton: TextView,
        confirmButton: TextView,
        dialogTitle: TextView,
        val currentStorageLocation: TextView,
        val modifyButton: TextView,
        val compressRecyclerView: RecyclerView
    ) : NameDialogFragment.Binding(
        root,
        groupButton,
        cancelButton,
        confirmButton,
        dialogTitle,
        nameLayout,
        nameEdit,
    ) {
        companion object {
            const val TAG = "ArchiveDialogFragment"

            fun inflate(inflater: LayoutInflater): Binding {
                val binding = CreateArchiveDialogBinding.inflate(inflater)
                val bindingRoot = binding.root
                val itemDialogButtonGroupBinding = ItemDialogButtonGroupBinding.bind(bindingRoot)
                val itemDialogTitleBinding = ItemDialogTitleBinding.bind(bindingRoot)
                val nameBinding = NameDialogNameIncludeBinding.bind(bindingRoot)
                return Binding(
                    bindingRoot,
                    nameBinding.nameLayout,
                    nameBinding.nameEdit,
                    itemDialogButtonGroupBinding.buttonGroup,
                    itemDialogButtonGroupBinding.buttonCancel,
                    itemDialogButtonGroupBinding.buttonConfirm,
                    itemDialogTitleBinding.dialogTitle,
                    binding.idCurrentStorageLocation,
                    binding.idModifyButton,
                    binding.compressRecycleView
                )
            }
        }
    }

    interface Listener : FileNameDialogFragment.Listener {
        fun archive(
            path: Path?,
            files: FileItemSet,
            name: String,
            archiveType: String,
            compressorType: String?
        )
    }
}

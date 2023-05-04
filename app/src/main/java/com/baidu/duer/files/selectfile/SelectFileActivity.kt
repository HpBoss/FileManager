package com.baidu.duer.files.selectfile

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.GridLayoutManager
import com.baidu.duer.files.R
import com.baidu.duer.files.app.AppActivity
import com.baidu.duer.files.databinding.ActivitySelectFileBinding
import com.baidu.duer.files.file.FileItem
import com.baidu.duer.files.filelist.BreadcrumbLayout
import com.baidu.duer.files.filelist.isListable
import com.baidu.duer.files.filelist.listablePath
import com.baidu.duer.files.navigation.createFromAsset
import com.baidu.duer.files.settings.Settings
import com.baidu.duer.files.util.*
import java8.nio.file.Path
import java8.nio.file.Paths

/**
 * @Author : 何飘
 * @CreateTime : 2023/2/10
 * @Description :
 */
class SelectFileActivity : AppActivity(), MoveFileListAdapter.Listener, BreadcrumbLayout.Listener {
    private lateinit var binding: ActivitySelectFileBinding
    private var mCurrentPath: Path = Paths.get(ALL_FILE)
    private val viewModel by viewModels<MoveFileViewModel>()
    private lateinit var mAdapter: MoveFileListAdapter
    private var filterDirectory: Array<String>? = null

    // 后续横屏、竖屏会动态改变
    private val pageSpanCount by lazy {
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 9 else 5
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_select_file)
        initView()
        intViewModel(this)
    }

    private fun initView() {
        mAdapter = MoveFileListAdapter(this, this)
        binding.includeSelectFileContent.recyclerView.apply {
            adapter = mAdapter
            // 使用GridLayoutManager时，Item的width最好设置成match_parent，可以保证所有Item内容居中显示
            layoutManager = GridLayoutManager(activity, pageSpanCount)
        }
        binding.selectFileBreadCrumbLayout.setListener(this)
        binding.idBackPageButton.setOnClickListener {
            if (!viewModel.navigateUp(false)) {
                finish()
            }
        }
        binding.idSelectFileTitle.text = intent.getStringExtra(TITLE)
        binding.idSelectFileTitle.typeface = createFromAsset()
        binding.idCompleteRightButton.apply {
            text = when (intent.getStringExtra(SELECT_TYPE)) {
                ARCHIVES -> getString(R.string.file_select_page_button_confirm)
                PASTE -> getString(R.string.paste)
                else -> getString(R.string.file_select_page_complete_button)
            }
            setOnClickListener {
                val intent = Intent()
                intent.putExtra(PATH, mCurrentPath.toString())
                setResult(RESULT_CODE, intent)
                finish()
            }
        }
        filterDirectory = intent.getStringArrayExtra(PATH_DATA)
    }

    private fun intViewModel(owner: LifecycleOwner) {
        viewModel.apply {
            resetTo(mCurrentPath)
            fileListLiveData.observe(owner) {
                onFileListChanged(it)
            }
            currentPathLiveData.observe(owner) {
                mCurrentPath = it
            }
            sortOptionsLiveData.observe(owner) {
                mAdapter.comparator = sortOptions.createComparator()
            }
            breadcrumbLiveData.observe(owner) {
                binding.selectFileBreadCrumbLayout.setData(it)
            }
        }
    }

    private fun onFileListChanged(stateful: Stateful<List<FileItem>>) {
        val files = stateful.value
        val hasFiles = !files.isNullOrEmpty()
        binding.includeSelectFileContent.progress.fadeToVisibilityUnsafe(stateful is Loading && !hasFiles)
        binding.includeSelectFileContent.errorText.fadeToVisibilityUnsafe(stateful is Failure && !hasFiles)
        val throwable = (stateful as? Failure)?.throwable
        if (throwable != null) {
            throwable.printStackTrace()
            val error = throwable.toString()
            if (hasFiles) {
                showToast(error)
            } else {
                binding.includeSelectFileContent.errorText.text = error
            }
        }
        binding.includeSelectFileContent.emptyView.fadeToVisibilityUnsafe(stateful is Success && !hasFiles)
        if (files != null) {
            updateAdapterFileList()
        } else {
            mAdapter.clear()
        }
        if (stateful is Success) {
            viewModel.pendingState
                ?.let {
                    binding.includeSelectFileContent.recyclerView.layoutManager!!.onRestoreInstanceState(
                        it
                    )
                }
        }
    }

    private fun updateAdapterFileList() {
        var files = viewModel.fileListStateful.value ?: return
        if (!Settings.FILE_LIST_SHOW_HIDDEN_FILES.valueCompat) {
            files = files.filterNot { it.isHidden }
        }
        mAdapter.replaceListAndIsSearching(
            // 在该页面显示的文件只能是文件夹，因为需要压缩的文件不能放入一个文件中
            // 在复制、移动、压缩文件操作时，路径选择页面过滤自身文件夹
            // 解压在选择路径时，没有传入需要过滤的路径，因为压缩文件就在选择页面看不到，此时filterDirectory为null
            files.filter {
                it.attributes.isDirectory && filterDirectory?.let { directoryArray ->
                    !directoryArray.contains(
                        it.path.toString()
                    )
                } ?: true
            }
        )
    }

    override fun navigateTo(path: Path) {
        Log.i(TAG, "navigateTo, path: $path")
        val state =
            binding.includeSelectFileContent.recyclerView.layoutManager!!.onSaveInstanceState()
        viewModel.navigateTo(state!!, path)
    }

    override fun copyPath(path: Path) {

    }

    override fun openInNewTask(path: Path) {

    }

    override fun openFile(file: FileItem) {
        Log.i(TAG, "openFile, file: $file")
        // 显示时就已经过滤了，保证只显示文件夹类型文件
        if (file.isListable) {
            navigateTo(file.listablePath)
        }
    }

    companion object {
        const val TAG = "MoveFileActivity"
    }
}
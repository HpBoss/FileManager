package com.baidu.duer.files.search

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.baidu.duer.files.R
import com.baidu.duer.files.app.AppActivity
import com.baidu.duer.files.databinding.ActivitySearchFileBinding
import com.baidu.duer.files.file.FileItem
import com.baidu.duer.files.filelist.FileListAdapter
import com.baidu.duer.files.filelist.FileSortOptions
import com.baidu.duer.files.filelist.ListType
import com.baidu.duer.files.settings.Settings
import com.baidu.duer.files.util.SEARCH_OPEN_FILE_RESULT_CODE
import com.baidu.duer.files.util.setOnEditorConfirmActionListener
import com.baidu.duer.files.util.valueCompat

/**
 * @Author : 何飘
 * @CreateTime : 2023/3/5
 * @Description :
 */
class SearchFileActivity : AppActivity(), TextWatcher, FileListAdapter.SimpleListener {
    private lateinit var binding: ActivitySearchFileBinding
    private lateinit var mAdapter: FileListAdapter
    private val viewModel by viewModels<SearchViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search_file)
        mAdapter = FileListAdapter(this, this)
        binding.apply {
            searchEdit.addTextChangedListener(this@SearchFileActivity)
            searchEdit.setOnEditorConfirmActionListener {
                val imm: InputMethodManager =
                    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(searchEdit.windowToken, 0)
            }

            searchBackButton.setOnClickListener { finish() }
            searchDelete.setOnClickListener {
                searchEdit.text?.clear()
                searchEdit.requestFocus()
                viewModel.stopSearching()
                mAdapter.clear()
                searchResultHint.text = null
            }
            searchRecycleView.layoutManager = LinearLayoutManager(this@SearchFileActivity)
            searchRecycleView.adapter = mAdapter
        }
        // Item排序方式：按名称升序排列，文件夹优先
        mAdapter.comparator = FileSortOptions(
            FileSortOptions.By.NAME,
            FileSortOptions.Order.ASCENDING,
            true
        ).createComparator()
        initViewModel()
    }

    private fun initViewModel() {
        val lifecycleOwner = this
        viewModel.apply {
            Settings.FILE_NAME_ELLIPSIZE.observe(lifecycleOwner) {
                mAdapter.nameEllipsize = it
            }
            searchFileListLiveData.observe(lifecycleOwner) {
                var files = it.value ?: return@observe
                if (!Settings.FILE_LIST_SHOW_HIDDEN_FILES.valueCompat) {
                    files = files.filterNot { it.isHidden }
                }
                binding.emptyView.visibility = if (files.isNotEmpty()) {
                    binding.searchResultHint.text =
                        getString(R.string.search_file_result_hint, searchState.query, files.size)
                    View.GONE
                } else {
                    if (searchState.isSearching) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }
                }
                mAdapter.replaceListAndIsSearching(
                    files,
                    viewModel.searchState.isSearching,
                    ListType.VERTICAL
                )
            }
        }
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        binding.searchResultHint.text = viewModel.searchResultHint.value
        viewModel.searchResultHint.value = null
    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        p0?.let {
            binding.searchHint.visibility = if (it.isNotEmpty()) View.GONE else View.VISIBLE
            binding.searchDelete.visibility = if (it.isNotEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun afterTextChanged(p0: Editable?) {
        if (p0 != null && p0.isNotEmpty()) {
            viewModel.search(p0.toString())
        } else {
            viewModel.stopSearching()
        }
    }

    override fun openFile(file: FileItem) {
        val intent = Intent()
        intent.putExtra("file", file)
        setResult(SEARCH_OPEN_FILE_RESULT_CODE, intent)
        finish()
    }

    override fun onStop() {
        super.onStop()
        viewModel.searchResultHint.value = binding.searchResultHint.text.toString()
    }
}
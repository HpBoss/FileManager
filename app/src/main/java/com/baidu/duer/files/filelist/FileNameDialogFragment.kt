package com.baidu.duer.files.filelist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.baidu.duer.files.R
import com.baidu.duer.files.util.FileName
import com.baidu.duer.files.util.PathName
import com.baidu.duer.files.util.asFileName
import com.baidu.duer.files.util.isValidFileName

abstract class FileNameDialogFragment : NameDialogFragment() {
    open val isArchiveFile = false

    override val listener: Listener
        get() = super.listener as Listener

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        val errorString = savedInstanceState?.getString(NAME_EXCEPTION_HINT)
        binding.nameLayout?.apply {
            if (errorString?.isNotEmpty() == true) {
                post {
                    isErrorEnabled = true
                    error = errorString
                }
            }
        }
        return view
    }

    override fun isNameValid(name: String): Boolean {
        if (!super.isNameValid(name)) {
            return false
        }
        val fileName = if (name.isValidFileName) name.asFileName() else null
        if (name.isEmpty() || fileName?.baseName?.isEmpty() == true && isArchiveFile) {
            binding.nameLayout?.error = getString(R.string.file_name_error_empty)
            return false
        }
        /*if (name.asFileNameOrNull() == null) {
            binding.nameLayout?.error = getString(R.string.file_name_error_invalid)
            return false
        }*/
        if (name.contains(PathName.SEPARATOR)) {
            binding.nameLayout?.error = getString(R.string.file_name_error_include_invalid_char)
            return false
        }
        if (name.startsWith(FileName.EXTENSION_SEPARATOR)) {
            binding.nameLayout?.error = getString(R.string.file_name_include_point_invalid)
            return false
        }
        // 这里只能比较当前路径下是否存在相同文件名的情况
        val listener = listener
        if (isCurrentPath() && listener.hasFileWithName(name)) {
            binding.nameLayout?.isErrorEnabled = true
            binding.nameLayout?.error = getString(R.string.file_name_error_already_exists)
            return false
        }
        return true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(NAME_EXCEPTION_HINT, binding.nameLayout?.error.toString())
    }

    open fun isCurrentPath(): Boolean = true

    interface Listener : NameDialogFragment.Listener {
        fun hasFileWithName(name: String): Boolean
    }

    companion object {
        private const val NAME_EXCEPTION_HINT = "name_exception_hint"
    }
}

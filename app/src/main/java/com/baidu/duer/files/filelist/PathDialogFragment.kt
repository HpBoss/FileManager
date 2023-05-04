package com.baidu.duer.files.filelist

import android.net.Uri
import com.baidu.duer.files.R
import com.baidu.duer.files.storage.createOrLog
import com.baidu.duer.files.util.asPathNameOrNull
import java8.nio.file.Path
import java8.nio.file.Paths
import java.net.URI

abstract class PathDialogFragment : NameDialogFragment() {
    override fun isNameValid(name: String): Boolean {
        if (!super.isNameValid(name)) {
            return false
        }
        if (name.isEmpty()) {
            binding.nameLayout?.error = getString(R.string.file_list_path_error_empty)
            return false
        }
        if (name.asPathNameOrNull() == null || name.toPathOrNull() == null) {
            binding.nameLayout?.error = getString(R.string.file_list_path_error_invalid)
            return false
        }
        return true
    }

    final override fun onOk(name: String) {
        onOk(name.toPathOrNull()!!)
    }

    private fun String.toPathOrNull(): Path? {
        val uri = URI::class.createOrLog(toString())
        // Also try to accept decoded path.
            ?: Uri.parse(this).let {
                URI::class.createOrLog(
                    it.scheme, it.userInfo, it.host, it.port, it.path, it.query, it.fragment
                )
            }
        if (uri != null) {
            try {
                return Paths.get(uri)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (startsWith('/')) {
            return Paths.get(this)
        }
        return null
    }

    abstract fun onOk(path: Path)
}

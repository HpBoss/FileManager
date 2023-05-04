package com.baidu.duer.files.filelist

import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContract
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import com.baidu.duer.files.app.AppActivity
import com.baidu.duer.files.file.MimeType
import com.baidu.duer.files.util.createIntent
import com.baidu.duer.files.util.extraPath
import com.baidu.duer.files.util.putArgs
import java8.nio.file.Path
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FileListActivity : AppActivity() {
    private lateinit var fragment: FileListFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Calls ensureSubDecor().
        findViewById<View>(android.R.id.content)
        if (savedInstanceState == null) {
            fragment = FileListFragment().putArgs(FileListFragment.Args(intent))
            supportFragmentManager.commit { add(android.R.id.content, fragment) }
        } else {
            fragment = supportFragmentManager.findFragmentById(android.R.id.content)
                    as FileListFragment
        }
    }

    override fun onResume() {
        super.onResume()
        // 文件管理器可见时扫描一遍新增文件
        lifecycleScope.launch(Dispatchers.IO) {
            MediaScannerConnection.scanFile(
                this@FileListActivity,
                arrayOf(Environment.getExternalStorageDirectory().absolutePath),
                null
            ) { p0, p1 ->
                Log.i("FileListActivity", "path: $p0, uri: $p1")
            }
        }
    }

    override fun onBackPressed() {
        if (fragment.onBackPressed()) {
            return
        }
        super.onBackPressed()
    }

    companion object {
        fun createViewIntent(path: Path): Intent =
            FileListActivity::class.createIntent()
                .setAction(Intent.ACTION_VIEW)
                .apply { extraPath = path }
    }

    class PickDirectoryContract : ActivityResultContract<Path?, Path?>() {
        override fun createIntent(context: Context, input: Path?): Intent =
            FileListActivity::class.createIntent()
                .setAction(Intent.ACTION_OPEN_DOCUMENT_TREE)
                .apply { input?.let { extraPath = it } }

        override fun parseResult(resultCode: Int, intent: Intent?): Path? =
            if (resultCode == RESULT_OK) intent?.extraPath else null
    }

    class PickFileContract : ActivityResultContract<List<MimeType>, Path?>() {
        override fun createIntent(context: Context, input: List<MimeType>): Intent =
            FileListActivity::class.createIntent()
                .setAction(Intent.ACTION_OPEN_DOCUMENT)
                .setType(MimeType.ANY.value)
                .putExtra(Intent.EXTRA_MIME_TYPES, input.map { it.value }.toTypedArray())

        override fun parseResult(resultCode: Int, intent: Intent?): Path? =
            if (resultCode == RESULT_OK) intent?.extraPath else null
    }
}

package com.baidu.duer.files.settings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.add
import androidx.fragment.app.commit
import com.baidu.duer.files.app.AppActivity

class BookmarkDirectoryListActivity : AppActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Calls ensureSubDecor().
        findViewById<View>(android.R.id.content)
        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                add<BookmarkDirectoryListFragment>(android.R.id.content)
            }
        }
    }
}

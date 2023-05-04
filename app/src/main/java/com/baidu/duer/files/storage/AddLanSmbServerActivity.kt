package com.baidu.duer.files.storage

import android.os.Bundle
import android.view.View
import androidx.fragment.app.commit
import com.baidu.duer.files.app.AppActivity

class AddLanSmbServerActivity : AppActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Calls ensureSubDecor().
        findViewById<View>(android.R.id.content)
        if (savedInstanceState == null) {
            supportFragmentManager.commit { add(android.R.id.content, AddLanSmbServerFragment()) }
        }
    }
}

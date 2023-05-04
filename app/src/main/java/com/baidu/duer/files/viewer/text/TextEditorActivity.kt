package com.baidu.duer.files.viewer.text

import android.os.Bundle
import android.view.View
import androidx.fragment.app.commit
import com.baidu.duer.files.app.AppActivity
import com.baidu.duer.files.util.putArgs

class TextEditorActivity : AppActivity() {
    private lateinit var fragment: TextEditorFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Calls ensureSubDecor().
        findViewById<View>(android.R.id.content)
        if (savedInstanceState == null) {
            fragment = TextEditorFragment().putArgs(TextEditorFragment.Args(intent))
            supportFragmentManager.commit { add(android.R.id.content, fragment) }
        } else {
            fragment = supportFragmentManager.findFragmentById(android.R.id.content)
                    as TextEditorFragment
        }
    }

    override fun onBackPressed() {
        if (fragment.onFinish()) {
            return
        }
        super.onBackPressed()
    }

    override fun onSupportNavigateUp(): Boolean {
        if (fragment.onFinish()) {
            return true
        }
        return super.onSupportNavigateUp()
    }
}

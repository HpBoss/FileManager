package com.baidu.duer.files.terminal

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.baidu.duer.files.util.startActivitySafe

object Terminal {
    fun open(path: String, context: Context) {
        val intent = Intent()
            .setComponent(ComponentName("jackpal.androidterm", "jackpal.androidterm.TermHere"))
            .setAction(Intent.ACTION_SEND)
            .putExtra(Intent.EXTRA_STREAM, Uri.parse(path))
        context.startActivitySafe(intent)
    }
}

package com.baidu.duer.files.ui

import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import com.baidu.duer.files.util.fadeInUnsafe
import com.baidu.duer.files.util.fadeOutUnsafe

class OverlayToolbarActionMode(bar: ViewGroup, toolbar: Toolbar) : ToolbarActionMode(bar, toolbar) {
    constructor(toolbar: Toolbar) : this(toolbar, toolbar)

    init {
        bar.isVisible = false
    }

    override fun show(bar: ViewGroup, animate: Boolean) {
        if (animate) {
            bar.fadeInUnsafe()
        } else {
            bar.isVisible = true
        }
    }

    override fun hide(bar: ViewGroup, animate: Boolean) {
        if (animate) {
            bar.fadeOutUnsafe()
        } else {
            bar.isVisible = false
        }
    }
}

package com.baidu.duer.files.ui

import android.content.Context
import android.util.AttributeSet
import android.view.WindowInsets
import androidx.annotation.AttrRes
import com.drakeet.drawer.FullDraggableContainer

class IgnoreFitsSystemWindowsFullyDraggableDrawerContentLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, @AttrRes defStyleAttr: Int = 0
) : FullDraggableContainer(context, attrs, defStyleAttr) {
    override fun onApplyWindowInsets(insets: WindowInsets): WindowInsets = insets
}

package com.baidu.duer.files.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.annotation.AttrRes
import com.baidu.duer.files.R
import com.baidu.duer.files.compat.getDrawableCompat
import com.google.android.material.textfield.TextInputLayout

class ReadOnlyTextInputLayout : TextInputLayout {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    )

    init {
        isExpandedHintEnabled = false
    }

    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams) {
        super.addView(child, index, params)

        if (child is EditText) {
            setDropDown(!child.isTextSelectable)
        }
    }

    fun setDropDown(dropDown: Boolean) {
        if (dropDown) {
            endIconMode = END_ICON_CUSTOM
            endIconDrawable = context.getDrawableCompat(R.drawable.mtrl_ic_arrow_drop_down)
        } else {
            endIconMode = END_ICON_NONE
        }
    }
}

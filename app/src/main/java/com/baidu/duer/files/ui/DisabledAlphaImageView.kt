package com.baidu.duer.files.ui

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.annotation.AttrRes
import androidx.appcompat.widget.AppCompatImageView
import com.baidu.duer.files.util.getFloatByAttr
import kotlin.math.roundToInt

class DisabledAlphaImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {
    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)

        updateImageAlpha()
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()

        updateImageAlpha()
    }

    private fun updateImageAlpha() {
        var alpha = 0xFF
        val enabled = android.R.attr.state_enabled in drawableState
        if (!enabled) {
            val disabledAlpha = context.getFloatByAttr(android.R.attr.disabledAlpha)
            alpha = (disabledAlpha * alpha).roundToInt()
        }
        imageAlpha = alpha
    }
}

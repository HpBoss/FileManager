package com.baidu.duer.files.ui

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.WindowInsets
import android.widget.FrameLayout
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.MaterialShapeUtils

class NavigationFrameLayout : FrameLayout {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    )

    constructor(
        context: Context,
        attrs: AttributeSet?,
        @AttrRes defStyleAttr: Int,
        @StyleRes defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    init {
        val background = background
        if (background is ColorDrawable) {
            this.background = MaterialShapeDrawable().apply {
                fillColor = ColorStateList.valueOf(background.color)
                initializeElevationOverlay(context)
                elevation = this@NavigationFrameLayout.elevation
            }
        }
    }

    var interceptClick: Boolean = false

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        MaterialShapeUtils.setParentAbsoluteElevation(this)
    }

    override fun setElevation(elevation: Float) {
        super.setElevation(elevation)

        MaterialShapeUtils.setElevation(this, elevation)
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        return if (interceptClick) true else super.dispatchTouchEvent(ev)
    }

    override fun onApplyWindowInsets(insets: WindowInsets): WindowInsets = insets
}

package com.baidu.duer.files.navigation

import android.content.Context
import android.util.AttributeSet
import android.view.WindowInsets
import androidx.annotation.AttrRes
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.RecyclerView
import com.baidu.duer.files.R
import com.baidu.duer.files.util.displayWidth
import com.baidu.duer.files.util.getDimensionPixelSize
import com.baidu.duer.files.util.getDimensionPixelSizeByAttr
import com.baidu.duer.files.util.isLayoutDirectionRtl

class NavigationRecyclerView : RecyclerView {
    private val verticalPadding = 0
    private val actionBarSize = context.getDimensionPixelSizeByAttr(R.attr.actionBarSize)
    private val maxWidth = context.getDimensionPixelSize(R.dimen.navigation_max_width)

    private var insetStart = 0
    private var insetTop = 0

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(
        context: Context,
        attrs: AttributeSet?,
        @AttrRes defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr)

    init {
        updatePadding(top = verticalPadding, bottom = verticalPadding)
        fitsSystemWindows = true
        setWillNotDraw(false)
    }

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        var widthSpec = widthSpec
        var width = (context.displayWidth - actionBarSize).coerceIn(0..insetStart + maxWidth)
        when (MeasureSpec.getMode(widthSpec)) {
            MeasureSpec.AT_MOST -> {
                width = width.coerceAtMost(MeasureSpec.getSize(widthSpec))
                widthSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY)
            }
            MeasureSpec.UNSPECIFIED ->
                widthSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY)
            MeasureSpec.EXACTLY -> {}
        }
        super.onMeasure(widthSpec, heightSpec)
    }

    override fun onApplyWindowInsets(insets: WindowInsets): WindowInsets {
        val isLayoutDirectionRtl = isLayoutDirectionRtl
        insetStart = if (isLayoutDirectionRtl) {
            insets.systemWindowInsetRight
        } else {
            insets.systemWindowInsetLeft
        }
        val paddingLeft = if (isLayoutDirectionRtl) 0 else insetStart
        val paddingRight = if (isLayoutDirectionRtl) insetStart else 0
        insetTop = insets.systemWindowInsetTop
        setPadding(
            paddingLeft, 0, paddingRight,
            verticalPadding + insets.systemWindowInsetBottom
        )
        requestLayout()
        return insets.replaceSystemWindowInsets(
            insets.systemWindowInsetLeft - paddingLeft, 0,
            insets.systemWindowInsetRight - paddingRight, 0
        )
    }
}

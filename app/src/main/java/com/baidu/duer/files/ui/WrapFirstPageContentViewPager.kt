package com.baidu.duer.files.ui

import android.content.Context
import android.util.AttributeSet
import androidx.viewpager.widget.ViewPager

class WrapFirstPageContentViewPager : ViewPager {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var heightMeasureSpec = heightMeasureSpec
        if (MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY && childCount > 0) {
            val child = getChildAt(0)
            child.measure(widthMeasureSpec, heightMeasureSpec)
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(
                child.measuredHeight, MeasureSpec.EXACTLY
            )
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }
}

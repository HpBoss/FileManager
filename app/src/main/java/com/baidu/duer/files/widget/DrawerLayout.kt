package com.baidu.duer.files.widget

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout

/**
 * @Author : 何飘
 * @CreateTime : 2023/4/10
 * @Description :
 */
class DrawerLayout @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : DrawerLayout(
    context!!, attrs, defStyle
) {
    private val mGestureDetector: GestureDetectorCompat?

    init {
        mGestureDetector = GestureDetectorCompat(context!!, object : SimpleOnGestureListener() {
            // 重写GestureDetector.SimpleOnGestureListener的onSingleTapUp方法
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                return super.onSingleTapUp(e)
            }
        })
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (mGestureDetector != null && mGestureDetector.onTouchEvent(ev)) {
            return true
        }
        val result = super.onInterceptTouchEvent(ev)
        if (ev.action == MotionEvent.ACTION_UP || ev.action == MotionEvent.ACTION_CANCEL) {
            closeDrawer(GravityCompat.START)
        }
        return result
    }
}
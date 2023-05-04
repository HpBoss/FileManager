package com.baidu.duer.files.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import com.baidu.duer.files.R
import com.github.penfeizhou.animation.loader.ResourceStreamLoader
import com.github.penfeizhou.animation.webp.WebPDrawable
import com.scwang.smart.refresh.layout.api.RefreshHeader
import com.scwang.smart.refresh.layout.api.RefreshKernel
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.constant.RefreshState
import com.scwang.smart.refresh.layout.constant.SpinnerStyle


/**
 * @Author : 何飘
 * @CreateTime : 2023/3/2
 * @Description :
 */
@SuppressLint("RestrictedApi")
class CommonLoadingView : LinearLayout, RefreshHeader {
    private var mLoadingView: ImageView? = null
    private var mTitleView: TextView? = null
    private var isShowTitle: Boolean = true

    constructor(context: Context?) : super(context) {
        initView()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        getAttribute(attrs)
        initView()
    }

    private fun getAttribute(attrs: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CommonLoadingView)
        try {
            isShowTitle = typedArray.getBoolean(R.styleable.CommonLoadingView_isShowTitle, true)
        } catch (exception: java.lang.Exception) {
            exception.printStackTrace()
        } finally {
            typedArray.recycle()
        }
    }

    private fun initView() {
        LayoutInflater.from(context)
            .inflate(R.layout.common_loading_dialog_horizontal, this, true)
        mLoadingView = findViewById(R.id.loading)
        if (isShowTitle) mTitleView = findViewById(R.id.title)
        mTitleView?.visibility = View.VISIBLE
        setLoadingDrawable(R.drawable.common_loading_dark)
    }

    private fun setLoadingDrawable(@DrawableRes drawableId: Int) {
        try {
            val resourceLoader = ResourceStreamLoader(context, drawableId)
            val webPDrawable = WebPDrawable(resourceLoader)
            mLoadingView?.setImageDrawable(webPDrawable)
        } catch (ex: Exception) {
            ex.localizedMessage?.let { Log.e(TAG, it) }
        }
    }

    override fun onStateChanged(
        refreshLayout: RefreshLayout,
        oldState: RefreshState,
        newState: RefreshState
    ) {
        when (newState) {
            RefreshState.PullDownToRefresh, RefreshState.Refreshing -> {
                mTitleView?.text = context.getString(R.string.common_loading_ongoing)
            }
            RefreshState.RefreshFinish -> {
                mTitleView?.text = context.getString(R.string.common_loading_complete)
            }
            else -> {}
        }
    }

    override fun getView(): View {
        return this
    }

    override fun getSpinnerStyle(): SpinnerStyle {
        return SpinnerStyle.Translate
    }

    override fun setPrimaryColors(vararg colors: Int) {

    }

    override fun onInitialized(kernel: RefreshKernel, height: Int, maxDragHeight: Int) {

    }

    override fun onMoving(
        isDragging: Boolean,
        percent: Float,
        offset: Int,
        height: Int,
        maxDragHeight: Int
    ) {

    }

    override fun onReleased(refreshLayout: RefreshLayout, height: Int, maxDragHeight: Int) {

    }

    override fun onStartAnimator(refreshLayout: RefreshLayout, height: Int, maxDragHeight: Int) {

    }

    override fun onFinish(refreshLayout: RefreshLayout, success: Boolean): Int {
        mTitleView?.apply {
            text = if (success) {
                context.getString(R.string.common_loading_complete)
            } else {
                context.getString(R.string.common_loading_failure)
            }
        }
        // 延迟500ms开始
        return 500
    }

    override fun onHorizontalDrag(percentX: Float, offsetX: Int, offsetMax: Int) {

    }

    override fun isSupportHorizontalDrag(): Boolean {
        return false
    }

    companion object {
        private const val TAG = "CommonLoadingView"
    }
}
package com.baidu.duer.files.dialog

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatDialogFragment
import com.baidu.duer.files.compat.createCornerDrawable

/**
 * @Author : 何飘
 * @CreateTime : 2023/3/10
 * @Description :
 */
abstract class BaseDialogFragment : AppCompatDialogFragment() {
    open val x: Int = 0

    open val y: Int = 0

    open val radius: Float = 0f

    open val gravity: Int = Gravity.CENTER

    abstract fun initData()

    open val width: Int = WindowManager.LayoutParams.WRAP_CONTENT

    open val height: Int = WindowManager.LayoutParams.WRAP_CONTENT

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.apply {
            val attributes = window?.attributes
            attributes?.apply {
                gravity = this@BaseDialogFragment.gravity
                x = this@BaseDialogFragment.x
                y = this@BaseDialogFragment.y
            }
            window?.apply {
                setDimAmount(0.5f)
                decorView.elevation = 0f
                setBackgroundDrawable(requireContext().createCornerDrawable(radius))
                setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                this.attributes = attributes
            }
        }
        initData()
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        // Dialog的width、height必须在Dialog创建成功后才可以设置成功
        val attributes = dialog?.window?.attributes
        attributes?.apply {
            width = this@BaseDialogFragment.width
            height = this@BaseDialogFragment.height
        }
        dialog?.window?.attributes = attributes
    }
}
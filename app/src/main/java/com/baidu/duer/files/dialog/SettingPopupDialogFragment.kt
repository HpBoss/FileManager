package com.baidu.duer.files.dialog

import android.os.Build
import android.view.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.baidu.duer.files.util.*

/**
 * @Author : 何飘
 * @CreateTime : 2023/3/5
 * @Description :
 */
class SettingPopupDialogFragment : BasePopupDialogFragment() {
    override val args by args<Args>()

    override val x: Int
        get() = dpToDimensionPixelOffset(65)

    override val y: Int
        get() = dpToDimensionPixelOffset(20)

    override val radius: Float
        get() = 16f

    override val gravity: Int
        get() = Gravity.TOP or Gravity.START

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onClick(index: Int) {
        listener.selectSettingMenu(args.sorWayList.getOrNull(index)?.name)
        super.onClick(index)
    }

    companion object {
        fun show(sorWayList: MutableList<SortWayItem>, fragment: Fragment) {
            SettingPopupDialogFragment().putArgs(Args(sorWayList)).show(fragment)
        }
    }
}

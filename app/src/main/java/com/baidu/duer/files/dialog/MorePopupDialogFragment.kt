package com.baidu.duer.files.dialog

import android.view.Gravity
import androidx.fragment.app.Fragment
import com.baidu.duer.files.util.args
import com.baidu.duer.files.util.dpToDimensionPixelOffset
import com.baidu.duer.files.util.putArgs
import com.baidu.duer.files.util.show

/**
 * @Author : 何飘
 * @CreateTime : 2023/3/5
 * @Description :
 */
class MorePopupDialogFragment : BasePopupDialogFragment() {
    override val args by args<Args>()

    override val x: Int
        get() = dpToDimensionPixelOffset(25)

    override val y: Int
        get() = dpToDimensionPixelOffset(22)

    override val radius: Float
        get() = 20f

    override val gravity: Int
        get() = Gravity.TOP or Gravity.END

    override fun onClick(index: Int) {
        listener.selectMoreMenu(args.sorWayList.getOrNull(index)?.name)
        super.onClick(index)
    }

    companion object {
        fun show(sorWayList: MutableList<SortWayItem>, fragment: Fragment) {
            MorePopupDialogFragment().putArgs(Args(sorWayList)).show(fragment)
        }
    }
}

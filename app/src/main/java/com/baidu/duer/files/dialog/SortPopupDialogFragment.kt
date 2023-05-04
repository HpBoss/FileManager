package com.baidu.duer.files.dialog

import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.baidu.duer.files.databinding.PopupDialogFragmentBinding
import com.baidu.duer.files.filelist.FileSortOptions
import com.baidu.duer.files.util.*
import kotlinx.parcelize.Parcelize

/**
 * @Author : 何飘
 * @CreateTime : 2023/3/4
 * @Description :
 */
class SortPopupDialogFragment : BaseDialogFragment(), ClickListener {
    private var selectIndex = 0
    private val args by args<Args>()
    private val listener: SortClickListener
        get() = requireParentFragment() as SortClickListener
    private val mAdapter: SortPopupDialogAdapter by lazy {
        SortPopupDialogAdapter(requireContext(), this, args.sorWayList)
    }
    private val binding by lazy {
        PopupDialogFragmentBinding.inflate(requireContext().layoutInflater)
    }

    override val x: Int
        get() = dpToDimensionPixelOffset(25)

    override val y: Int
        get() = dpToDimensionPixelOffset(25)

    override val radius: Float
        get() = 16f

    override val gravity: Int
        get() = Gravity.TOP or Gravity.END

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        for (index in args.sorWayList.indices) {
            if (args.sorWayList[index].isChecked) {
                selectIndex = index
                break
            }
        }
        return binding.root
    }

    override fun initData() {
        binding.apply {
            popupRecyclerView.apply {
                isNestedScrollingEnabled = false
                layoutManager = LinearLayoutManager(requireContext())
                adapter = mAdapter
                mAdapter.notifyItemRangeChanged(0, args.sorWayList.size)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onClick(index: Int) {
        // 重复点击时才改变状态
        if (index == selectIndex) {
            args.sorWayList.getOrNull(index)?.isAscend =
                !(args.sorWayList.getOrNull(index)?.isAscend ?: true)
        }
        if (index != selectIndex) {
            args.sorWayList.getOrNull(selectIndex)?.isChecked = false
            args.sorWayList.getOrNull(index)?.isChecked = true
            selectIndex = index
        }
        mAdapter.notifyItemRangeChanged(0, args.sorWayList.size, args.sorWayList)
        listener.onSortClick(
            sortTypeMap.getOrDefault(selectIndex, FileSortOptions.By.NAME),
            if (args.sorWayList.getOrNull(selectIndex)?.isAscend != false)
                FileSortOptions.Order.ASCENDING
            else FileSortOptions.Order.DESCENDING
        )
    }

    @Parcelize
    class Args(val sorWayList: MutableList<SortWayItem>) : ParcelableArgs

    companion object {
        fun show(sorWayList: MutableList<SortWayItem>, fragment: Fragment) {
            SortPopupDialogFragment().putArgs(Args(sorWayList)).show(fragment)
        }
    }

    interface SortClickListener {
        fun onSortClick(sortType: FileSortOptions.By, orderType: FileSortOptions.Order)
    }
}

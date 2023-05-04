package com.baidu.duer.files.dialog

import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.baidu.duer.files.databinding.PopupDialogFragmentBinding
import com.baidu.duer.files.util.ParcelableArgs
import com.baidu.duer.files.util.layoutInflater
import kotlinx.parcelize.Parcelize

/**
 * @Author : 何飘
 * @CreateTime : 2023/3/5
 * @Description :
 */
abstract class BasePopupDialogFragment : BaseDialogFragment(), ClickListener {
    protected val listener: SimpleClickListener
        get() = requireParentFragment() as SimpleClickListener

    private val mAdapter: SimplePopupDialogAdapter by lazy {
        SimplePopupDialogAdapter(requireContext(), this, args.sorWayList)
    }

    private val binding by lazy {
        PopupDialogFragmentBinding.inflate(requireContext().layoutInflater)
    }

    abstract val args: Args

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
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
        dismiss()
    }

    @Parcelize
    class Args(val sorWayList: MutableList<SortWayItem>) : ParcelableArgs

    interface ClickListener {
        fun selectMoreMenu(name: String?)
        fun selectSettingMenu(name: String?)
    }

    interface SimpleClickListener : ClickListener {
        override fun selectMoreMenu(name: String?) {}
        override fun selectSettingMenu(name: String?) {}
    }
}
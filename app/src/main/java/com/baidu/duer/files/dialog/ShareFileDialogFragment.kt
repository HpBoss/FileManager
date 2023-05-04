package com.baidu.duer.files.dialog

import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.baidu.duer.files.R
import com.baidu.duer.files.databinding.ShareOpenDialogFragmentBinding
import com.baidu.duer.files.file.MimeType
import com.baidu.duer.files.navigation.getShareApps
import com.baidu.duer.files.util.*
import kotlinx.parcelize.Parcelize

/**
 * @Author : 何飘
 * @CreateTime : 2023/3/8
 * @Description :
 */
class ShareFileDialogFragment : BaseDialogFragment(), ClickListener {
    private var mCurrentSelectPosition = 0
    private val args by args<Args>()
    private lateinit var mAdapter: ShareViewPagerAdapter
    private val resolveInfoList by lazy {
        getShareApps(args.uris as ArrayList<Uri>, args.mimeTypes).toMutableList()
    }
    private val binding by lazy {
        ShareOpenDialogFragmentBinding.inflate(requireContext().layoutInflater)
    }

    override val width: Int
        get() = dpToDimensionPixelOffset(608)

    override val radius: Float
        get() = 12f

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
            dialogTitle.text = getString(R.string.file_share_dialog_title, args.uris.size)
            // 去除viewPager2边缘的阴影（在xml中设置overScrollMode为never无效）
            val child: View = viewPager.getChildAt(0)
            (child as? RecyclerView)?.overScrollMode = View.OVER_SCROLL_NEVER
            val pageCount = resolveInfoList.size / 10 + if (resolveInfoList.size % 10 > 0) 1 else 0
            createIndicator(pageCount)
            viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    if (pageCount > 1) {
                        indicate.getChildAt(mCurrentSelectPosition).isEnabled = false
                        indicate.getChildAt(position).isEnabled = true
                    }
                    mCurrentSelectPosition = position
                }
            })
            val arrayListResult = arrayListOf<MutableList<ResolveInfo?>?>()
            repeat(pageCount) {
                if (it * PAGE_COUNT == resolveInfoList.size) return@repeat
                val trailIndex =
                    if (resolveInfoList.size - PAGE_COUNT * it >= PAGE_COUNT) {
                        PAGE_COUNT * it + PAGE_COUNT
                    } else {
                        resolveInfoList.size
                    }
                arrayListResult.add(resolveInfoList.subList(it * PAGE_COUNT, trailIndex))
            }
            mAdapter =
                ShareViewPagerAdapter(
                    requireContext(),
                    this@ShareFileDialogFragment,
                    arrayListResult
                )
            viewPager.adapter = mAdapter
        }
    }

    override fun onClick(index: Int) {
        resolveInfoList.getOrNull(index)?.let {
            startActivity(
                (args.uris as? ArrayList<Uri>)?.createSendStreamShareIntent(args.mimeTypes, it)
            )
        }
        dismiss()
    }

    private fun createIndicator(size: Int) {
        if (size <= 1) return
        binding.indicate.removeAllViews()
        repeat(size) {
            // 创建底部指示器(小圆点)
            val view = View(requireContext()).apply {
                setBackgroundResource(R.drawable.indicator)
                isEnabled = false
            }
            val layoutParams = LinearLayout.LayoutParams(8.px, 8.px).apply {
                leftMargin = 8.px
                rightMargin = 8.px
            }
            binding.indicate.addView(view, layoutParams)
        }
        binding.indicate.getChildAt(0)?.isEnabled = true
    }

    @Parcelize
    class Args(val mimeTypes: List<MimeType>, val uris: List<Uri>) : ParcelableArgs

    companion object {
        const val PAGE_COUNT = 10
        fun show(mimeTypes: List<MimeType>, uris: List<Uri>, fragment: Fragment) {
            ShareFileDialogFragment().putArgs(Args(mimeTypes, uris)).show(fragment)
        }
    }
}
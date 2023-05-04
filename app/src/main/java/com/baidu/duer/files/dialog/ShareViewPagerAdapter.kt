package com.baidu.duer.files.dialog

import android.content.Context
import android.content.pm.ResolveInfo
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.baidu.duer.files.R
import com.baidu.duer.files.databinding.ItemApplicationRecyclviewBinding

/**
 * @Author : 何飘
 * @CreateTime : 2023/3/15
 * @Description :
 */
class ShareViewPagerAdapter(
    private val context: Context,
    private val listener: ClickListener,
    private val resolveInfoList: MutableList<MutableList<ResolveInfo?>?>
) : RecyclerView.Adapter<ShareViewPagerAdapter.ShareViewPagerHolder>() {
    private val innerAdapterList: ArrayList<ShareDialogAdapter> = ArrayList()
        get() {
            resolveInfoList.forEachIndexed { page, resolveInfo ->
                field.add(ShareDialogAdapter(context, listener, resolveInfo, page))
            }
            return field
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShareViewPagerHolder {
        val binding: ItemApplicationRecyclviewBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.item_application_recyclview,
            parent,
            false
        )
        return ShareViewPagerHolder(binding.root)
    }

    override fun onBindViewHolder(holder: ShareViewPagerHolder, position: Int) {
        if (resolveInfoList.size == 0) return
        val binding: ItemApplicationRecyclviewBinding? = DataBindingUtil.getBinding(holder.itemView)
        binding?.recyclerView?.apply {
            isNestedScrollingEnabled = false
            layoutManager = GridLayoutManager(context, SPAN_COUNT)
            adapter = innerAdapterList[position]
            innerAdapterList[position].notifyItemRangeChanged(
                0,
                resolveInfoList[position]?.size ?: 0
            )
        }
    }

    override fun getItemCount(): Int {
        return resolveInfoList.size
    }

    inner class ShareViewPagerHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    companion object {
        const val SPAN_COUNT = 5
    }
}

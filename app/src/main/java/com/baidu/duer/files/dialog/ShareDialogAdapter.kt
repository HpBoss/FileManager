package com.baidu.duer.files.dialog

import android.content.Context
import android.content.pm.ResolveInfo
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.baidu.duer.files.R
import com.baidu.duer.files.app.packageManager
import com.baidu.duer.files.databinding.ItemApplicationLayoutBinding

/**
 * @Author : 何飘
 * @CreateTime : 2023/3/8
 * @Description :
 */
class ShareDialogAdapter(
    private val context: Context,
    val listener: ClickListener,
    private val resolveInfoList: MutableList<ResolveInfo?>?,
    private val page: Int
) : RecyclerView.Adapter<ShareDialogAdapter.ApplicationInfoHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApplicationInfoHolder {
        val binding: ItemApplicationLayoutBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.item_application_layout,
            parent,
            false
        )
        return ApplicationInfoHolder(binding.root)
    }

    override fun onBindViewHolder(holder: ApplicationInfoHolder, position: Int) {
        if (resolveInfoList?.size == 0) return
        val binding: ItemApplicationLayoutBinding? = DataBindingUtil.getBinding(holder.itemView)
        binding?.apply {
            itemName = resolveInfoList?.getOrNull(position)?.loadLabel(packageManager).toString()
            applicationIcon.setImageDrawable(
                resolveInfoList?.getOrNull(position)?.loadIcon(packageManager)
            )
            itemApplicationLayout.setOnClickListener { listener.onClick(position + page * ShareFileDialogFragment.PAGE_COUNT) }
            executePendingBindings()
        }
    }

    override fun getItemCount(): Int {
        return resolveInfoList?.size ?: 0
    }

    inner class ApplicationInfoHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
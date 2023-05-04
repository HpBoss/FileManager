package com.baidu.duer.files.dialog

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.baidu.duer.files.R
import com.baidu.duer.files.databinding.ItemSortInfoBinding
import com.baidu.duer.files.navigation.createFromAsset

/**
 * @Author : 何飘
 * @CreateTime : 2023/3/4
 * @Description :
 */
class SortPopupDialogAdapter(
    private val context: Context,
    val listener: ClickListener,
    private val sortWayList: MutableList<SortWayItem>
) : RecyclerView.Adapter<SortPopupDialogAdapter.SortInfoHolder>() {
    private val itemColor = ColorStateList(
        arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
        intArrayOf(
            ContextCompat.getColor(context, R.color.dialog_text_color),
            ContextCompat.getColor(context, R.color.bread_crumb_path_text_normal_color)
        )
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SortInfoHolder {
        val binding: ItemSortInfoBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.item_sort_info,
            parent,
            false
        )
        return SortInfoHolder(binding.root).apply {
            binding.itemTitle.typeface = createFromAsset()
        }
    }

    override fun onBindViewHolder(holder: SortInfoHolder, position: Int) {
        if (sortWayList.size == 0) return
        val binding: ItemSortInfoBinding? = DataBindingUtil.getBinding(holder.itemView)
        binding?.apply {
            itemName = sortWayList[position].name
            itemSortLayout.isChecked = sortWayList[position].isChecked
            itemSortLayout.setOnClickListener { listener.onClick(position) }
            itemTitle.setTextColor(itemColor)
            sortWayIcon.setImageResource(
                if (sortWayList[position].isChecked) {
                    if (sortWayList[position].isAscend) {
                        R.drawable.sort_ascend_icon
                    } else {
                        R.drawable.sort_descend_icon
                    }
                } else {
                    R.drawable.sort_normal_icon
                }
            )
            executePendingBindings()
        }
    }

    override fun getItemCount(): Int {
        return sortWayList.size
    }

    inner class SortInfoHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
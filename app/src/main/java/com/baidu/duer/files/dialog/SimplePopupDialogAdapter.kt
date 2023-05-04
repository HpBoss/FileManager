package com.baidu.duer.files.dialog

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.baidu.duer.files.R
import com.baidu.duer.files.databinding.ItemSimpleInfoBinding
import com.baidu.duer.files.navigation.createFromAsset
import com.baidu.duer.files.util.px


/**
 * @Author : 何飘
 * @CreateTime : 2023/3/5
 * @Description :
 */
class SimplePopupDialogAdapter(
    private val context: Context,
    val listener: ClickListener,
    private val sortWayList: MutableList<SortWayItem>
) : RecyclerView.Adapter<SimplePopupDialogAdapter.SimpleInfoHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleInfoHolder {
        val binding: ItemSimpleInfoBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.item_simple_info,
            parent,
            false
        )
        return SimpleInfoHolder(binding.root).apply {
            binding.itemTitle.typeface = createFromAsset()
        }
    }

    override fun onBindViewHolder(holder: SimpleInfoHolder, position: Int) {
        if (sortWayList.size == 0) return
        val binding: ItemSimpleInfoBinding? = DataBindingUtil.getBinding(holder.itemView)
        binding?.apply {
            itemName = sortWayList[position].name
            itemSortLayout.isChecked = sortWayList[position].isChecked
            itemSortLayout.setOnClickListener { listener.onClick(position) }
            val lp = itemSortLayout.layoutParams as RecyclerView.LayoutParams
            lp.topMargin = if (position == 0) 34.px else 36.px
            lp.bottomMargin = if (position == sortWayList.size - 1) 34.px else 0
            itemSortLayout.layoutParams = lp
            executePendingBindings()
        }
    }

    override fun getItemCount(): Int {
        return sortWayList.size
    }

    inner class SimpleInfoHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
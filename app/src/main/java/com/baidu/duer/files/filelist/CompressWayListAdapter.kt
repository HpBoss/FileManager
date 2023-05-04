package com.baidu.duer.files.filelist

import android.content.Context
import android.content.res.ColorStateList
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.baidu.duer.files.R
import com.baidu.duer.files.databinding.CompressSelectItemBinding
import com.baidu.duer.files.dialog.ClickListener
import com.baidu.duer.files.dialog.CompressWayItem
import com.baidu.duer.files.ui.SimpleAdapter
import com.baidu.duer.files.util.layoutInflater

class CompressWayListAdapter(
    val listener: ClickListener,
    val context: Context
) : SimpleAdapter<CompressWayItem?, RecyclerView.ViewHolder>() {
    private val itemColor = ColorStateList(
        arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
        intArrayOf(
            ContextCompat.getColor(context, android.R.color.white),
            ContextCompat.getColor(context, R.color.bread_crumb_path_text_normal_color)
        )
    )

    fun notifyCheckedChanged() {
        notifyItemRangeChanged(0, itemCount, PAYLOAD_CHECKED_CHANGED)
    }

    override val hasStableIds: Boolean
        get() = true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ItemHolder(
            CompressSelectItemBinding.inflate(parent.context.layoutInflater, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        throw UnsupportedOperationException()
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: List<Any>
    ) {
        val item = getItem(position)
        val binding = (holder as ItemHolder).binding
        binding.itemCompressLayout.isChecked = item?.isChecked ?: false
        binding.compressWayName.setTextColor(itemColor)
        if (payloads.isNotEmpty()) return
        binding.itemCompressLayout.setOnClickListener {
            listener.onClick(position)
        }
        binding.compressWayName.text = item?.compressName
    }

    companion object {
        private val PAYLOAD_CHECKED_CHANGED = Any()
    }

    private class ItemHolder(val binding: CompressSelectItemBinding) : RecyclerView.ViewHolder(
        binding.root
    )
}

package com.baidu.duer.files.navigation

import android.content.Context
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.baidu.duer.files.databinding.NavigationItemBinding
import com.baidu.duer.files.ui.SimpleAdapter
import com.baidu.duer.files.util.layoutInflater
import com.baidu.duer.files.util.px

class NavigationListAdapter(
    private val listener: NavigationItem.Listener,
    val context: Context
) : SimpleAdapter<NavigationItem?, RecyclerView.ViewHolder>() {

    fun notifyCheckedChanged() {
        notifyItemRangeChanged(0, itemCount, PAYLOAD_CHECKED_CHANGED)
    }

    override val hasStableIds: Boolean
        get() = true

    override fun getItemId(position: Int): Long =
        getItem(position)?.id ?: list.subList(0, position).count { it == null }.toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ItemHolder(
            NavigationItemBinding.inflate(parent.context.layoutInflater, parent, false)
        ).apply {
            binding.titleText.typeface = createFromAsset()
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        throw UnsupportedOperationException()
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: List<Any>
    ) {
        val item = getItem(position)!!
        val binding = (holder as ItemHolder).binding
        binding.itemLayout.isChecked = item.isChecked(listener)
        if (payloads.isNotEmpty()) {
            return
        }
        binding.itemLayout.setOnClickListener {
            item.onClick(listener, binding.itemLayout.context)
        }
        binding.itemLayout.setOnLongClickListener { item.onLongClick(listener) }
        binding.iconImage.setImageDrawable(item.getIcon(binding.iconImage.context))
        val lp = binding.iconImage.layoutParams as LinearLayout.LayoutParams
        val itemLp = binding.itemLayout.layoutParams as RecyclerView.LayoutParams
        if (item.getIcon(binding.iconImage.context) != null) {
            lp.marginEnd = 10.px
        } else {
            lp.marginEnd = 0
        }
        binding.titleText.text = item.getTitle(binding.titleText.context)
        binding.subtitleText.text = item.getSubtitle(binding.subtitleText.context)

    }

    companion object {
        private val PAYLOAD_CHECKED_CHANGED = Any()
    }

    private class ItemHolder(val binding: NavigationItemBinding) : RecyclerView.ViewHolder(
        binding.root
    )
}

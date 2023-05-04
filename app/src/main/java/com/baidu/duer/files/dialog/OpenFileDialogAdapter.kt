package com.baidu.duer.files.dialog

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.baidu.duer.files.app.packageManager
import com.baidu.duer.files.databinding.ItemOpenApplicationLayoutBinding
import com.baidu.duer.files.ui.SimpleAdapter
import com.baidu.duer.files.util.layoutInflater
import com.baidu.duer.files.util.px

/**
 * @Author : 何飘
 * @CreateTime : 2023/3/8
 * @Description :
 */
class OpenFileDialogAdapter(
    val listener: ClickListener
) : SimpleAdapter<ResolveInfoCheck?, RecyclerView.ViewHolder>() {

    override val hasStableIds: Boolean
        get() = true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApplicationInfoHolder {
        return ApplicationInfoHolder(
            ItemOpenApplicationLayoutBinding.inflate(parent.context.layoutInflater, parent, false)
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
        val binding = (holder as ApplicationInfoHolder).binding
        binding.apply {
            itemApplicationLayout.isChecked = item?.isChecked ?: false
            if (payloads.isNotEmpty()) return
            val lp = itemApplicationLayout.layoutParams as RecyclerView.LayoutParams
            if (position == 0) {
                lp.marginStart = 50.px
                lp.marginEnd = 15.px
            }
            if (position == itemCount - 1) {
                lp.marginStart = 15.px
                lp.marginEnd = 50.px
            }
            if (position != 0 && position != itemCount - 1) {
                lp.marginStart = 15.px
                lp.marginEnd = 15.px
            }
            itemApplicationLayout.layoutParams = lp
            applicationName.text = item?.resolveInfo?.loadLabel(packageManager).toString()
            applicationIcon.setImageDrawable(item?.resolveInfo?.loadIcon(packageManager))
            itemApplicationLayout.setOnClickListener { listener.onClick(position) }
        }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    inner class ApplicationInfoHolder(val binding: ItemOpenApplicationLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)
}
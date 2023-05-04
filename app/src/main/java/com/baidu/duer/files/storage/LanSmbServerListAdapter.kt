package com.baidu.duer.files.storage

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.baidu.duer.files.databinding.LanSmbServerItemBinding
import com.baidu.duer.files.ui.SimpleAdapter
import com.baidu.duer.files.util.layoutInflater

class LanSmbServerListAdapter(
    val listener: (LanSmbServer) -> Unit
) : SimpleAdapter<LanSmbServer, LanSmbServerListAdapter.ViewHolder>() {
    override val hasStableIds: Boolean
        get() = true

    override fun getItemId(position: Int): Long = getItem(position).hashCode().toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(LanSmbServerItemBinding.inflate(parent.context.layoutInflater, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val server = getItem(position)
        val binding = holder.binding
        binding.itemLayout.setOnClickListener { listener(server) }
        binding.hostText.text = server.host
        binding.addressText.text = server.address.hostAddress
    }

    class ViewHolder(val binding: LanSmbServerItemBinding) : RecyclerView.ViewHolder(binding.root)
}

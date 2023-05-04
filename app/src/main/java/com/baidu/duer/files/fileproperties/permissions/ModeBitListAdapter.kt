package com.baidu.duer.files.fileproperties.permissions

import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.baidu.duer.files.databinding.ModeBitItemBinding
import com.baidu.duer.files.provider.common.PosixFileModeBit
import com.baidu.duer.files.util.layoutInflater

class ModeBitListAdapter(
    private val modeBits: List<PosixFileModeBit>,
    private val modeBitNames: Array<String>
) : BaseAdapter() {
    var mode: Set<PosixFileModeBit> = emptySet()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getCount(): Int = modeBits.size

    override fun getItem(position: Int): PosixFileModeBit = modeBits[position]

    override fun hasStableIds(): Boolean = true

    override fun getItemId(position: Int): Long = getItem(position).ordinal.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val modeBit = getItem(position)
        val binding = convertView?.tag as ModeBitItemBinding?
            ?: ModeBitItemBinding.inflate(parent.context.layoutInflater, parent, false)
                .apply { root.tag = this }
        binding.modeBitCheck.text = modeBitNames[position]
        binding.modeBitCheck.isChecked = modeBit in mode
        return binding.root
    }
}

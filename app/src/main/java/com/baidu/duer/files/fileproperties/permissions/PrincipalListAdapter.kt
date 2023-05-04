package com.baidu.duer.files.fileproperties.permissions

import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.RecyclerView
import coil.dispose
import coil.load
import com.baidu.duer.files.R
import com.baidu.duer.files.coil.ignoreError
import com.baidu.duer.files.compat.getDrawableCompat
import com.baidu.duer.files.databinding.PrincipalItemBinding
import com.baidu.duer.files.ui.SimpleAdapter
import com.baidu.duer.files.util.SelectionLiveData
import com.baidu.duer.files.util.layoutInflater

abstract class PrincipalListAdapter(
    private val selectionLiveData: SelectionLiveData<Int>
) : SimpleAdapter<PrincipalItem, PrincipalListAdapter.ViewHolder>() {
    override val hasStableIds: Boolean
        get() = true

    override fun getItemId(position: Int): Long = getItem(position).id.toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(PrincipalItemBinding.inflate(parent.context.layoutInflater, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        throw UnsupportedOperationException()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: List<Any>) {
        val principal = getItem(position)
        val binding = holder.binding
        binding.itemLayout.isChecked = selectionLiveData.value == principal.id
        if (payloads.isNotEmpty()) {
            return
        }
        binding.itemLayout.setOnClickListener {
            selectionLiveData.setValue(principal.id)
        }
        val icon = binding.iconImage.context.getDrawableCompat(principalIconRes)
        val applicationInfo = principal.applicationInfos.firstOrNull()
        if (applicationInfo != null) {
            binding.iconImage.load(applicationInfo) {
                placeholder(icon)
                ignoreError()
            }
        } else {
            binding.iconImage.dispose()
            binding.iconImage.setImageDrawable(icon)
        }
        binding.principalText.text = if (principal.name != null) {
            binding.principalText.context.getString(
                R.string.file_properties_permissions_principal_format, principal.name, principal.id
            )
        } else {
            principal.id.toString()
        }
        binding.labelText.text = principal.applicationLabels.firstOrNull()
            ?: binding.labelText.resources.getString(
                R.string.file_properties_permissions_set_principal_system
            )
    }

    @get:DrawableRes
    protected abstract val principalIconRes: Int

    fun findPositionByPrincipalId(id: Int): Int = findPositionById(id.toLong())

    class ViewHolder(val binding: PrincipalItemBinding) : RecyclerView.ViewHolder(binding.root)
}

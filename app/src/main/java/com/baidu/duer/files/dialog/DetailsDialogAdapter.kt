package com.baidu.duer.files.dialog

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.baidu.duer.files.R
import com.baidu.duer.files.databinding.ItemFileDataInfoBinding
import com.baidu.duer.files.navigation.createFromAsset

/**
 * @Author : 何飘
 * @CreateTime : 2023/2/14
 * @Description :
 */
class DetailsDialogAdapter(
    private val context: Context
) : RecyclerView.Adapter<DetailsDialogAdapter.FileInfoHolder>() {
    private var fileInfoList: MutableList<FileInfo> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileInfoHolder {
        val binding: ItemFileDataInfoBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.item_file_data_info,
            parent,
            false
        )
        return FileInfoHolder(binding.root).apply {
            binding.idInfoContent.typeface = createFromAsset()
        }
    }

    override fun onBindViewHolder(holder: FileInfoHolder, position: Int) {
        if (fileInfoList.size == 0) return
        val binding: ItemFileDataInfoBinding? = DataBindingUtil.getBinding(holder.itemView)
        binding?.infoData = fileInfoList[position]
        binding?.executePendingBindings()
    }

    override fun getItemCount(): Int {
        return fileInfoList.size
    }

    fun updateInfoListData(fileInfoList: MutableList<FileInfo>) {
        this.fileInfoList = fileInfoList
        notifyItemRangeChanged(0, fileInfoList.size)
    }

    inner class FileInfoHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
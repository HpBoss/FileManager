package com.baidu.duer.files.selectfile

import android.content.Context
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.viewbinding.ViewBinding
import com.baidu.duer.files.R
import com.baidu.duer.files.database.AppDatabase
import com.baidu.duer.files.databinding.FileGridItemBinding
import com.baidu.duer.files.file.FileItem
import com.baidu.duer.files.file.fileSize
import com.baidu.duer.files.file.iconRes
import com.baidu.duer.files.filelist.ListType
import com.baidu.duer.files.filelist.fileItemSetOf
import com.baidu.duer.files.filelist.name
import com.baidu.duer.files.navigation.createFromAsset
import com.baidu.duer.files.settings.Settings
import com.baidu.duer.files.ui.AnimatedListAdapter
import com.baidu.duer.files.util.layoutInflater
import com.baidu.duer.files.util.valueCompat
import java8.nio.file.Path
import java8.nio.file.attribute.BasicFileAttributes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import me.zhanghai.android.fastscroll.PopupTextProvider
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import java.util.*

class MoveFileListAdapter(
    private val listener: Listener, private val context: Context
) : AnimatedListAdapter<FileItem, MoveFileListAdapter.BaseViewHolder>(CALLBACK), PopupTextProvider {
    private var isSearching = false
    private val coroutineScope by lazy { CoroutineScope(Dispatchers.Main) }

    private lateinit var _comparator: Comparator<FileItem>
    var comparator: Comparator<FileItem>
        get() = _comparator
        set(value) {
            _comparator = value
            if (!isSearching) {
                super.replace(list.sortedWith(value), true)
                rebuildFilePositionMap()
            }
        }

    private val selectedFiles = fileItemSetOf()

    private val filePositionMap = mutableMapOf<Path, Int>()

    override fun clear() {
        super.clear()

        rebuildFilePositionMap()
    }

    @Deprecated("", ReplaceWith("replaceListAndSearching(list, searching)"))
    override fun replace(list: List<FileItem>, clear: Boolean) {
        throw UnsupportedOperationException()
    }

    fun replaceListAndIsSearching(list: List<FileItem>) {
        // 在"最近更新"栏目中文件不按文件名排序，按照最后文件修改时间递减排序
        super.replace(list.sortedWith(comparator), true)
        rebuildFilePositionMap()
    }

    private fun rebuildFilePositionMap() {
        filePositionMap.clear()
        for (index in 0 until itemCount) {
            val file = getItem(index)
            filePositionMap[file.path] = index
        }
    }

    override fun getItemViewType(position: Int): Int {
        // 固定以Horizontal样式布局
        return ListType.HORIZONTAL
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        HorizontalViewHolder(
            FileGridItemBinding.inflate(parent.context.layoutInflater, parent, false)
        ).apply {
            binding.idTextFileName.typeface = createFromAsset()
        }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        throw UnsupportedOperationException()
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int, payloads: List<Any>) {
        val file = getItem(position)
        val isDirectory = file.attributes.isDirectory
        onBindHorizontalViewHolder(holder as HorizontalViewHolder, file, isDirectory, payloads)
    }

    private fun onBindHorizontalViewHolder(
        holder: HorizontalViewHolder,
        file: FileItem,
        isDirectory: Boolean,
        payloads: List<Any>,
    ) {
        val binding = holder.binding
        binding.apply {
            if (payloads.isNotEmpty()) return
            val attributes = file.attributes
            iconImage.setImageResource(file.mimeType.iconRes)
            idTextFileName.text = file.name
            val localDateTime =
                attributes.lastModifiedTime().toInstant().atZone(ZoneId.systemDefault())
                    .toLocalDateTime()
            idTextFileModifyTime.text =
                localDateTime.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
            idTextFileDescription.text = getFileDescription(file, isDirectory, attributes)
            horizontalItemLayout.isChecked = file in selectedFiles
            itemCheckBadgeImage.isVisible = selectedFiles.isNotEmpty()
            coroutineScope.launch {
                idCollectIcon.isVisible =
                    AppDatabase.getDatabase(context)?.collectDao()
                        ?.getCollect(file.path.toString()) != null
            }
            horizontalItemLayout.setOnClickListener {
                listener.openFile(file)
            }
        }
    }

    private fun getFileDescription(
        file: FileItem,
        isDirectory: Boolean,
        attributes: BasicFileAttributes
    ): CharSequence {
        return if (isDirectory) {
            val fileList = file.path.toFile().listFiles()
            var size = fileList?.size
            size?.let {
                fileList?.forEach {
                    if (it.name.startsWith(".")) {
                        size = size?.minus(1)
                    }
                }
            }
            context.getString(R.string.folder_description, size)
        } else {
            attributes.fileSize.formatHumanReadable(context)
        }
    }

    override fun getPopupText(position: Int): String {
        val file = getItem(position)
        return file.name.take(1).uppercase(Locale.getDefault())
    }

    override fun onViewRecycled(holder: BaseViewHolder) {
        super.onViewRecycled(holder)
        coroutineScope.cancel()
    }

    override val isAnimationEnabled: Boolean
        get() = Settings.FILE_LIST_ANIMATION.valueCompat

    companion object {
        private val PAYLOAD_STATE_CHANGED = Any()

        private val CALLBACK = object : DiffUtil.ItemCallback<FileItem>() {
            override fun areItemsTheSame(oldItem: FileItem, newItem: FileItem): Boolean =
                oldItem.path == newItem.path

            override fun areContentsTheSame(oldItem: FileItem, newItem: FileItem): Boolean =
                oldItem == newItem
        }
    }

    open class BaseViewHolder(bind: ViewBinding) : ViewHolder(bind.root)

    inner class HorizontalViewHolder(val binding: FileGridItemBinding) : BaseViewHolder(binding)

    interface Listener {
        fun openFile(file: FileItem)
    }
}

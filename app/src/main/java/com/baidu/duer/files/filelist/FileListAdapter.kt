package com.baidu.duer.files.filelist

import android.content.Context
import android.text.TextUtils
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.viewbinding.ViewBinding
import coil.dispose
import coil.load
import com.baidu.duer.files.R
import com.baidu.duer.files.assort.FileRecentListLiveData
import com.baidu.duer.files.database.AppDatabase
import com.baidu.duer.files.databinding.FileGridItemBinding
import com.baidu.duer.files.databinding.FileItemBinding
import com.baidu.duer.files.databinding.FileRecentBottomNoticeContentBinding
import com.baidu.duer.files.databinding.FileRecentTitleLayoutBinding
import com.baidu.duer.files.file.FileItem
import com.baidu.duer.files.file.fileSize
import com.baidu.duer.files.file.iconRes
import com.baidu.duer.files.navigation.createFromAsset
import com.baidu.duer.files.provider.archive.isArchivePath
import com.baidu.duer.files.provider.common.isReadable
import com.baidu.duer.files.settings.Settings
import com.baidu.duer.files.ui.AnimatedListAdapter
import com.baidu.duer.files.util.layoutInflater
import com.baidu.duer.files.util.px
import com.baidu.duer.files.util.valueCompat
import java8.nio.file.Path
import java8.nio.file.attribute.BasicFileAttributes
import kotlinx.coroutines.*
import me.zhanghai.android.fastscroll.PopupTextProvider
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import java.util.*

class FileListAdapter(
    private val listener: Listener, private val context: Context
) : AnimatedListAdapter<FileItem, FileListAdapter.BaseViewHolder>(CALLBACK), PopupTextProvider {
    private var type: Int = ListType.NONE
    private var isSearching = false
    private var firstSelect = false
    private val coroutineScope by lazy { CoroutineScope(Dispatchers.IO) }
    private val collectDao by lazy { AppDatabase.getDatabase(context)?.collectDao() }

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

    var pickOptions: PickOptions? = null
        set(value) {
            field = value
            notifyItemRangeChanged(0, itemCount, PAYLOAD_STATE_CHANGED)
        }

    private val selectedFiles = fileItemSetOf()

    private val filePositionMap = mutableMapOf<Path, Int>()

    private lateinit var _nameEllipsize: TextUtils.TruncateAt
    var nameEllipsize: TextUtils.TruncateAt
        get() = _nameEllipsize
        set(value) {
            _nameEllipsize = value
            notifyItemRangeChanged(0, itemCount, PAYLOAD_STATE_CHANGED)
        }

    fun replaceSelectedFiles(files: FileItemSet) {
        val changedFiles = fileItemSetOf()
        val iterator = selectedFiles.iterator()
        // 从选择到不选择，有这变化的Item进行更新
        while (iterator.hasNext()) {
            val file = iterator.next()
            if (file !in files) {
                iterator.remove()
                changedFiles.add(file)
            }
        }
        // 从不选择到选择，有这变化的Item也进行更新
        for (file in files) {
            if (file !in selectedFiles) {
                selectedFiles.add(file)
                changedFiles.add(file)
            }
        }
        // 首次长按文件列表，激活单选、全选，每个Item都需要更新UI，展示可全选
        // 如果不是则只更新本次改变的Item
        // 如果一键取消多选、单选时，也需要更新所有Item
        if (!firstSelect && selectedFiles.iterator().hasNext()) {
            for (file in changedFiles) {
                val position = filePositionMap[file.path]
                position?.let { notifyItemChanged(it, PAYLOAD_STATE_CHANGED) }
            }
        } else {
            notifyItemRangeChanged(0, itemCount, PAYLOAD_STATE_CHANGED)
        }
    }

    private fun selectFile(file: FileItem, firstSelect: Boolean = false) {
        if (!isFileSelectable(file)) {
            return
        }
        this.firstSelect = firstSelect
        val selected = file in selectedFiles
        val pickOptions = pickOptions
        if (!selected && pickOptions != null && !pickOptions.allowMultiple) {
            listener.clearSelectedFiles()
        }
        listener.selectFile(file, !selected)
    }

    fun selectAllFiles() {
        val files = fileItemSetOf()
        for (index in 0 until itemCount) {
            val file = getItem(index)
            if (isFileSelectable(file)) {
                files.add(file)
            }
        }
        listener.selectFiles(files, true)
    }

    fun collectFiles(files: FileItemSet) {
        files.forEach { file ->
            val position = filePositionMap[file.path]
            position?.let { notifyItemChanged(it, file) }
        }
    }

    private fun isFileSelectable(file: FileItem): Boolean {
        val pickOptions = pickOptions ?: return true
        return if (pickOptions.pickDirectory) {
            file.attributes.isDirectory
        } else {
            !file.attributes.isDirectory && pickOptions.mimeTypes.any { it.match(file.mimeType) }
        }
    }

    override fun clear() {
        super.clear()

        rebuildFilePositionMap()
    }

    @Deprecated("", ReplaceWith("replaceListAndSearching(list, searching)"))
    override fun replace(list: List<FileItem>, clear: Boolean) {
        throw UnsupportedOperationException()
    }

    fun replaceListAndIsSearching(list: List<FileItem>, isSearching: Boolean, type: Int) {
        val clear = this.isSearching != isSearching
        this.isSearching = isSearching
        this.type = type
        // 在"最近更新"栏目中文件不按文件名排序，按照最后文件修改时间递减排序
        super.replace(
            if (!isSearching && type != ListType.RECENT) list.sortedWith(comparator) else list,
            clear
        )
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
        return if (getItem(position).fileModifyDate.isNotEmpty()) {
            ListType.TITLE
        } else if (getItem(position).otherType == FileRecentListLiveData.BOTTOM_NOTICE) {
            ListType.BOTTOM_NOTICE
        } else {
            type
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        when (viewType) {
            ListType.VERTICAL -> {
                VerticalViewHolder(
                    FileItemBinding.inflate(parent.context.layoutInflater, parent, false)
                ).apply {
                    binding.nameText.typeface = createFromAsset()
                    /* Item选中后的背景色
                    binding.itemLayout.background =
                        CheckableItemBackground.create(binding.itemLayout.context)*/
                    popupMenu = PopupMenu(binding.menuButton.context, binding.menuButton)
                        .apply { inflate(R.menu.file_item) }
                }
            }
            ListType.RECENT -> {
                RecentContentViewHolder(
                    FileGridItemBinding.inflate(parent.context.layoutInflater, parent, false)
                ).apply {
                    binding.idTextFileName.typeface = createFromAsset()
                }
            }
            ListType.TITLE -> {
                RecentTitleViewHolder(
                    FileRecentTitleLayoutBinding.inflate(
                        parent.context.layoutInflater,
                        parent,
                        false
                    )
                ).apply {
                    binding.idDateText.typeface = createFromAsset()
                }
            }
            ListType.BOTTOM_NOTICE -> {
                RecentBottomNoticeViewHolder(
                    FileRecentBottomNoticeContentBinding.inflate(
                        parent.context.layoutInflater,
                        parent,
                        false
                    )
                )
            }
            // Horizontal
            else -> {
                HorizontalViewHolder(
                    FileGridItemBinding.inflate(parent.context.layoutInflater, parent, false)
                ).apply {
                    binding.idTextFileName.typeface = createFromAsset()
                }
            }
        }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        throw UnsupportedOperationException()
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int, payloads: List<Any>) {
        val file = getItem(position)
        val isDirectory = file.attributes.isDirectory
        val enabled = isFileSelectable(file) || isDirectory
        when (holder) {
            is VerticalViewHolder -> {
                onBindVerticalViewHolder(holder, enabled, file, payloads, isDirectory, position)
            }
            is HorizontalViewHolder -> {
                onBindHorizontalViewHolder(holder, file, payloads, isDirectory, position)
            }
            is RecentContentViewHolder -> {
                onBindRecentContentViewHolder(holder, file, payloads, isDirectory)
            }
            is RecentTitleViewHolder -> {
                onBindRecentTitleViewHolder(holder, file, payloads, position)
            }
        }
    }

    private fun onBindRecentTitleViewHolder(
        holder: RecentTitleViewHolder,
        file: FileItem,
        payloads: List<Any>,
        position: Int
    ) {
        if (payloads.isNotEmpty()) return
        val binding = holder.binding
        binding.apply {
            idDateText.text = file.fileModifyDate
        }
    }

    private fun onBindRecentContentViewHolder(
        holder: RecentContentViewHolder,
        file: FileItem,
        payloads: List<Any>,
        isDirectory: Boolean
    ) {
        val binding = holder.binding
        binding.apply {
            itemCheckBadgeImage.isVisible = false
            idTextFileModifyTime.isVisible = false
            coroutineScope.launch {
                val isVisible = collectDao?.getCollect(file.path.toString()) != null
                withContext(Dispatchers.Main) {
                    idCollectIcon.isVisible = isVisible
                }
            }
            if (payloads.isNotEmpty()) return
            val attributes = file.attributes
            iconImage.setImageResource(file.mimeType.iconRes)
            iconImage.isVisible = true
            idTextFileName.text = file.name
            idTextFileDescription.text = getFileDescription(file, isDirectory, attributes)
            thumbnailImage.dispose()
            thumbnailImage.setImageDrawable(null)
            val supportsThumbnail = file.supportsThumbnail
            thumbnailImage.isVisible = supportsThumbnail
            if (supportsThumbnail) {
                thumbnailImage.load(file.path to attributes) {
                    listener { _, _ -> iconImage.isVisible = false }
                }
            }
            horizontalItemLayout.setOnClickListener {
                if (selectedFiles.isEmpty()) {
                    listener.openFile(file)
                }
            }
        }
    }

    private fun onBindHorizontalViewHolder(
        holder: HorizontalViewHolder,
        file: FileItem,
        payloads: List<Any>,
        isDirectory: Boolean,
        position: Int
    ) {
        val binding = holder.binding
        binding.apply {
            horizontalItemLayout.isChecked = file in selectedFiles
            itemCheckBadgeImage.isVisible = selectedFiles.isNotEmpty()
            coroutineScope.launch {
                val isVisible = collectDao?.getCollect(file.path.toString()) != null
                withContext(Dispatchers.Main) {
                    idCollectIcon.isVisible = isVisible
                }
            }
            if (payloads.isNotEmpty()) return
            val lp = binding.horizontalItemLayout.layoutParams as RecyclerView.LayoutParams
            lp.bottomMargin = if (position == itemCount - 1) 20.px else 0
            val attributes = file.attributes
            idTextFileName.text = file.name
            val localDateTime =
                attributes.lastModifiedTime().toInstant().atZone(ZoneId.systemDefault())
                    .toLocalDateTime()
            idTextFileModifyTime.text =
                localDateTime.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
            idTextFileDescription.text = getFileDescription(file, isDirectory, attributes)
            iconImage.setImageResource(file.mimeType.iconRes)
            iconImage.isVisible = true
            thumbnailImage.dispose()
            thumbnailImage.setImageDrawable(null)
            val supportsThumbnail = file.supportsThumbnail
            thumbnailImage.isVisible = supportsThumbnail
            if (supportsThumbnail) {
                thumbnailImage.load(file.path to attributes) {
                    listener { _, _ -> iconImage.isVisible = false }
                }
            }
            horizontalItemLayout.setOnClickListener {
                if (selectedFiles.isEmpty()) {
                    listener.openFile(file)
                } else {
                    selectFile(file)
                }
            }
            horizontalItemLayout.setOnLongClickListener {
                if (selectedFiles.isEmpty()) {
                    selectFile(file, true)
                }
                true
            }
        }
    }

    private fun onBindVerticalViewHolder(
        holder: VerticalViewHolder,
        enabled: Boolean,
        file: FileItem,
        payloads: List<Any>,
        isDirectory: Boolean,
        position: Int
    ) {
        val binding = holder.binding
        binding.itemLayout.isEnabled = enabled
        binding.menuButton.isEnabled = enabled
        val menu = holder.popupMenu.menu
        val path = file.path
        val hasPickOptions = pickOptions != null
        val isReadOnly = path.fileSystem.isReadOnly
        menu.findItem(R.id.action_cut).isVisible = !hasPickOptions && !isReadOnly
        menu.findItem(R.id.action_copy).isVisible = !hasPickOptions
        val checked = file in selectedFiles
        binding.itemLayout.isChecked = checked
        binding.itemCheckBadgeImage.isVisible = selectedFiles.isNotEmpty()
        coroutineScope.launch {
            val isVisible = collectDao?.getCollect(file.path.toString()) != null
            withContext(Dispatchers.Main) {
                binding.idCollectIcon.isVisible = isVisible
            }
        }
        if (payloads.isNotEmpty()) {
            return
        }
        val lp = binding.itemLayout.layoutParams as RecyclerView.LayoutParams
        when (position) {
            0 -> {
                lp.topMargin = 4.px
                lp.bottomMargin = 0
            }
            itemCount - 1 -> {
                lp.topMargin = 0
                lp.bottomMargin = 20.px
            }
            else -> {
                lp.topMargin = 0
                lp.bottomMargin = 0
            }
        }
        binding.itemLayout.layoutParams = lp
        // bindViewHolderAnimation(holder)
        binding.itemLayout.setOnClickListener {
            if (selectedFiles.isEmpty()) {
                listener.openFile(file)
            } else {
                selectFile(file)
            }
        }
        binding.itemLayout.setOnLongClickListener {
            // 处于搜索状态时仅开放点击操作
            if (!isSearching) {
                if (selectedFiles.isEmpty()) {
                    selectFile(file, true)
                } else {
                    listener.openFile(file)
                }
            }
            true
        }
        binding.iconImage.setImageResource(file.mimeType.iconRes)
        binding.iconImage.isVisible = true
        binding.thumbnailImage.dispose()
        binding.thumbnailImage.setImageDrawable(null)
        val supportsThumbnail = file.supportsThumbnail
        binding.thumbnailImage.isVisible = supportsThumbnail
        val attributes = file.attributes
        if (supportsThumbnail) {
            binding.thumbnailImage.load(path to attributes) {
                listener { _, _ -> binding.iconImage.isVisible = false }
            }
        }
        binding.nameText.text = file.name
        binding.descriptionText.text = getFileDescription(file, isDirectory, attributes)
        /*
        移除最后修改时间显示
        binding.descriptionText.text = context.resources.getString(
            R.string.file_list_description,
            attributes.lastModifiedTime().toInstant().atZone(ZoneId.systemDefault())
                .toLocalDateTime()
                .format(DateTimeFormatter.ofPattern("yyyy/MM/dd")),
            getFileDescription(file, isDirectory, attributes)
        )
        */
        val isArchivePath = path.isArchivePath
        menu.findItem(R.id.action_copy)
            .setTitle(if (isArchivePath) R.string.file_item_action_extract else R.string.copy)
        menu.findItem(R.id.action_delete).isVisible = !isReadOnly
        menu.findItem(R.id.action_rename).isVisible = !isReadOnly
        menu.findItem(R.id.action_extract).isVisible = file.isArchiveFile
        menu.findItem(R.id.action_archive).isVisible = !isArchivePath
        menu.findItem(R.id.action_add_bookmark).isVisible = isDirectory
        holder.popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_open_with -> {
                    listener.openFileWith(file)
                    true
                }
                R.id.action_cut -> {
                    listener.cutFile(file)
                    true
                }
                R.id.action_copy -> {
                    listener.copyFile(file)
                    true
                }
                R.id.action_delete -> {
                    listener.confirmDeleteFile(file)
                    true
                }
                R.id.action_rename -> {
                    listener.showRenameFileDialog(file)
                    true
                }
                R.id.action_extract -> {
                    listener.extractFile(file)
                    true
                }
                R.id.action_archive -> {
                    listener.showCreateArchiveDialog(file)
                    true
                }
                R.id.action_share -> {
                    listener.shareFile(file)
                    true
                }
                R.id.action_copy_path -> {
                    listener.copyPath(file)
                    true
                }
                R.id.action_add_bookmark -> {
                    listener.addBookmark(file)
                    true
                }
                R.id.action_create_shortcut -> {
                    listener.createShortcut(file)
                    true
                }
                R.id.action_properties -> {
                    listener.showPropertiesDialog(file)
                    true
                }
                else -> false
            }
        }
    }

    private fun getFileDescription(
        file: FileItem,
        isDirectory: Boolean,
        attributes: BasicFileAttributes
    ): CharSequence {
        return if (file.path.isReadable) {
            if (isDirectory) {
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
        } else {
            context.getString(R.string.file_access_restrictions)
        }
    }

    override fun getPopupText(position: Int): String {
        val file = getItem(position)
        return file.name.take(1).uppercase(Locale.getDefault())
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

    inner class VerticalViewHolder(val binding: FileItemBinding) : BaseViewHolder(binding) {
        lateinit var popupMenu: PopupMenu
    }

    inner class HorizontalViewHolder(val binding: FileGridItemBinding) : BaseViewHolder(binding)

    inner class RecentContentViewHolder(val binding: FileGridItemBinding) : BaseViewHolder(binding)

    inner class RecentTitleViewHolder(val binding: FileRecentTitleLayoutBinding) :
        BaseViewHolder(binding)

    inner class RecentBottomNoticeViewHolder(val binding: FileRecentBottomNoticeContentBinding) :
        BaseViewHolder(binding)

    interface Listener {
        fun clearSelectedFiles()
        fun selectFile(file: FileItem, selected: Boolean)
        fun selectFiles(files: FileItemSet, selected: Boolean)
        fun openFile(file: FileItem)
        fun openFileWith(file: FileItem)
        fun cutFile(file: FileItem)
        fun copyFile(file: FileItem)
        fun confirmDeleteFile(file: FileItem)
        fun showRenameFileDialog(file: FileItem)
        fun extractFile(file: FileItem)
        fun showCreateArchiveDialog(file: FileItem)
        fun shareFile(file: FileItem)
        fun copyPath(file: FileItem)
        fun addBookmark(file: FileItem)
        fun createShortcut(file: FileItem)
        fun showPropertiesDialog(file: FileItem)
    }

    interface SimpleListener : Listener {
        override fun clearSelectedFiles() {}
        override fun selectFile(file: FileItem, selected: Boolean) {}
        override fun selectFiles(files: FileItemSet, selected: Boolean) {}
        override fun openFile(file: FileItem) {}
        override fun openFileWith(file: FileItem) {}
        override fun cutFile(file: FileItem) {}
        override fun copyFile(file: FileItem) {}
        override fun confirmDeleteFile(file: FileItem) {}
        override fun showRenameFileDialog(file: FileItem) {}
        override fun extractFile(file: FileItem) {}
        override fun showCreateArchiveDialog(file: FileItem) {}
        override fun shareFile(file: FileItem) {}
        override fun copyPath(file: FileItem) {}
        override fun addBookmark(file: FileItem) {}
        override fun createShortcut(file: FileItem) {}
        override fun showPropertiesDialog(file: FileItem) {}
    }
}

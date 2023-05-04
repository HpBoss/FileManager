package com.baidu.duer.files.navigation

import android.content.Context
import com.baidu.duer.files.filelist.TabType
import com.baidu.duer.files.storage.Storage

class StorageItem(
    private val storage: Storage
) : PathItem(storage.path, TabType.ALL), NavigationRoot {
    init {
        require(storage.isVisible)
    }

    override val id: Long
        get() = storage.id

    override fun getIconRes(): Int? = null

    override fun getTitle(context: Context): String = storage.getName(context)

    override fun getSubtitle(context: Context): String? =
        storage.linuxPath?.let { getMainStorageStats(context) }

    override fun onLongClick(listener: Listener): Boolean {
        return true
    }

    override fun getName(context: Context): String = getTitle(context)
}
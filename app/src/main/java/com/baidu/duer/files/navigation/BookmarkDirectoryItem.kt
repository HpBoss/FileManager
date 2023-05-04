package com.baidu.duer.files.navigation

import android.content.Context
import com.baidu.duer.files.R

/**
 * @Author : 何飘
 * @CreateTime : 2023/2/15
 * @Description :
 */
class BookmarkDirectoryItem(
    private val bookmarkDirectory: BookmarkDirectory
) : PathItem(bookmarkDirectory.path) {
    // We cannot simply use super.getId() because different bookmark directories may have
    // the same path.
    override val id: Long
        get() = bookmarkDirectory.id

    override fun getIconRes(): Int? = R.drawable.directory_icon_white_24dp

    override fun getTitle(context: Context): String = bookmarkDirectory.name

    override fun onLongClick(listener: Listener): Boolean {
        listener.onEditBookmarkDirectory(bookmarkDirectory)
        return true
    }
}
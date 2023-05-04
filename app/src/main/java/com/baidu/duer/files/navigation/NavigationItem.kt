package com.baidu.duer.files.navigation

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import com.baidu.duer.files.compat.getDrawableCompat
import com.baidu.duer.files.storage.Storage
import java8.nio.file.Path
import kotlinx.coroutines.CoroutineScope

abstract class NavigationItem {
    abstract val id: Long

    fun getIcon(context: Context): Drawable? = getIconRes()?.let { context.getDrawableCompat(it) }

    open fun getIconRes(): Int? = null

    abstract fun getTitle(context: Context): String

    open fun getSubtitle(context: Context): String? = null

    open fun isChecked(listener: Listener): Boolean = false

    open fun isRecentChecked(): Boolean = false

    abstract fun onClick(listener: Listener, context: Context)

    open fun onLongClick(listener: Listener): Boolean = false

    protected var coroutineScope: CoroutineScope? = null

    interface Listener {
        val currentPath: Path
        val currentTabType: Int
        fun navigateTo(path: Path)
        fun navigateToRoot(path: Path, tabType: Int)
        fun browseContent(tabType: Int)
        fun onAddStorage()
        fun onEditStorage(storage: Storage)
        fun onEditStandardDirectory(standardDirectory: StandardDirectory)
        fun onEditBookmarkDirectory(bookmarkDirectory: BookmarkDirectory)
        fun closeNavigationDrawer()
        fun startActivity(intent: Intent)
    }
}

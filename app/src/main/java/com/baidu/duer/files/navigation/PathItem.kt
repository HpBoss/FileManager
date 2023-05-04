package com.baidu.duer.files.navigation

import android.content.Context
import com.baidu.duer.files.app.application
import com.baidu.duer.files.filelist.TabType
import com.baidu.duer.files.util.isOrientationPortrait
import com.baidu.duer.files.util.tabNameMap
import java8.nio.file.Path
import kotlinx.coroutines.*

abstract class PathItem(val path: Path, private val tabType: Int = TabType.NONE) :
    NavigationItem() {
    override fun isChecked(listener: Listener): Boolean = listener.currentTabType == tabType

    override fun onClick(listener: Listener, context: Context) {
        tabNameMap[tabType] = getTitle(context)
        listener.closeNavigationDrawer()
        coroutineScope?.cancel()
        coroutineScope = CoroutineScope(Dispatchers.IO)
        coroutineScope?.apply {
            launch {
                if (application.isOrientationPortrait) delay(300L)
                withContext(Dispatchers.Main) {
                    if (this@PathItem is NavigationRoot) {
                        listener.navigateToRoot(path, tabType)
                    } else {
                        listener.navigateTo(path)
                    }
                }
            }
        }
    }
}
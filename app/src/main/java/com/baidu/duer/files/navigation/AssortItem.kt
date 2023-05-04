package com.baidu.duer.files.navigation

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.baidu.duer.files.app.application
import com.baidu.duer.files.util.isOrientationPortrait
import com.baidu.duer.files.util.tabNameMap
import kotlinx.coroutines.*

/**
 * @Author : 何飘
 * @CreateTime : 2023/2/15
 * @Description :
 */
open class AssortItem(
    @DrawableRes val icon: Int? = null,
    @StringRes val titleRes: Int,
    private val tabType: Int
) : NavigationItem() {
    override val id: Long
        get() = titleRes.hashCode().toLong()

    override fun getIconRes(): Int? {
        return icon
    }

    override fun isChecked(listener: Listener): Boolean = listener.currentTabType == tabType

    override fun getTitle(context: Context): String = context.getString(titleRes)

    override fun onClick(listener: Listener, context: Context) {
        tabNameMap[tabType] = getTitle(context)
        listener.closeNavigationDrawer()
        coroutineScope?.cancel()
        coroutineScope = CoroutineScope(Dispatchers.IO)
        coroutineScope?.apply {
            launch {
                if (application.isOrientationPortrait) delay(300L)
                withContext(Dispatchers.Main) {
                    listener.browseContent(tabType)
                }
            }
        }
    }
}
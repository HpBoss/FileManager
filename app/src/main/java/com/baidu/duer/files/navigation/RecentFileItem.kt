package com.baidu.duer.files.navigation

import android.content.Context
import com.baidu.duer.files.R
import com.baidu.duer.files.filelist.TabType
import com.baidu.duer.files.util.DEFAULT_PATH
import java8.nio.file.Paths

/**
 * @Author : 何飘
 * @CreateTime : 2023/2/15
 * @Description :
 */
class RecentFileItem : PathItem(Paths.get(DEFAULT_PATH), TabType.RECENT), NavigationRoot {
    override val id: Long
        get() = R.string.storage_file_recent_update_title.hashCode().toLong()

    override fun getTitle(context: Context): String =
        context.getString(R.string.storage_file_recent_update_title)

    override fun getName(context: Context): String = getTitle(context)
}
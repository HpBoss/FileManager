package com.baidu.duer.files.navigation

import android.content.Context
import java8.nio.file.Paths

/**
 * @Author : 何飘
 * @CreateTime : 2023/2/15
 * @Description :
 */
class StandardDirectoryItem(
    private val standardDirectory: StandardDirectory
) : PathItem(
    Paths.get(getExternalStorageDirectory(standardDirectory.relativePath)),
    standardDirectory.tabType
), NavigationRoot {
    init {
        require(standardDirectory.isEnabled)
    }

    override val id: Long
        get() = standardDirectory.id

    override fun getIconRes(): Int? = standardDirectory.iconRes

    override fun getTitle(context: Context): String = standardDirectory.getTitle(context)

    override fun getName(context: Context): String = getTitle(context)
}
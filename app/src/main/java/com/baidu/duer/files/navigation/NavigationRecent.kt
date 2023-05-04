package com.baidu.duer.files.navigation

import android.content.Context
import java8.nio.file.Path

/**
 * @Author : 何飘
 * @CreateTime : 2023/1/11
 * @Description :
 */
interface NavigationRecent {
    val path: Path

    fun getName(context: Context): String
}
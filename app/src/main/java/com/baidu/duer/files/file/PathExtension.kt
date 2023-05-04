package com.baidu.duer.files.file

import com.baidu.duer.files.provider.archive.isArchivePath
import java8.nio.file.Path

/**
 * @Author : 何飘
 * @CreateTime : 2023/3/7
 * @Description :
 */
fun Path.compare(other: Path?): Int {
    return if (!this.isArchivePath) {
        other?.let {
            this.compareTo(it)
        } ?: 0
    } else {
        0
    }
}
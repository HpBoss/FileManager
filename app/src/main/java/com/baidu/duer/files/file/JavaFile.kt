package com.baidu.duer.files.file

import java.io.File

object JavaFile {
    fun isDirectory(path: String): Boolean = File(path).isDirectory

    fun getFreeSpace(path: String): Long = File(path).freeSpace

    fun getTotalSpace(path: String): Long = File(path).totalSpace
}

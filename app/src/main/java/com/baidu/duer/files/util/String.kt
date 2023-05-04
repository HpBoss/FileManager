package com.baidu.duer.files.util

fun String.getFilenameFromPath() = substring(lastIndexOf("/") + 1)
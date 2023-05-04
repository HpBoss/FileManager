package com.baidu.duer.files.filelist

import android.content.Context
import java8.nio.file.Path

data class BreadcrumbData(
    val paths: List<Path>,
    val nameProducers: List<(Context) -> String>,
    val selectedIndex: Int
) {
    override fun toString(): String {
        return "BreadcrumbData(paths=$paths, nameProducers=$nameProducers, selectedIndex=$selectedIndex)"
    }
}

package com.baidu.duer.files.dialog

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * @Author : 何飘
 * @CreateTime : 2023/3/4
 * @Description : 默认升序、默认不选中
 */
@Parcelize
data class SortWayItem(
    val name: String,
    var isAscend: Boolean = true,
    var isChecked: Boolean = false
) : Parcelable
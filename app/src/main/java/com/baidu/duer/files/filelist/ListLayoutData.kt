package com.baidu.duer.files.filelist

/**
 * @Author : 何飘
 * @CreateTime : 2023/2/17
 * @Description :
 */
data class ListLayoutData(
    var tabType: Int,
    val layout: ArrayList<Boolean>,
    var selectedIndex: Int
) {
    override fun toString(): String {
        return "ListLayoutData(tabType=$tabType, layout=$layout, selectedIndex=$selectedIndex)"
    }
}

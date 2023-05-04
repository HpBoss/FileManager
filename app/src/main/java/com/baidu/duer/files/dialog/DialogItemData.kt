package com.baidu.duer.files.dialog

import com.baidu.duer.files.filelist.FileSortOptions


/**
 * @Author : 何飘
 * @CreateTime : 2023/3/5
 * @Description :
 */

// TODO String文本，待写入string.xml，语言适配删除后完成
val fileSortList = arrayListOf(
    SortWayItem("按名称"),
    SortWayItem("按大小"),
    SortWayItem("按时间"),
    SortWayItem("按类型"),
)

val sortTypeMap = mapOf(
    0 to FileSortOptions.By.NAME,
    1 to FileSortOptions.By.SIZE,
    2 to FileSortOptions.By.CREATE_TIME,
    3 to FileSortOptions.By.TYPE
)

enum class MoreMenu(val value: String) {
    ARCHIVE("压缩"),
    COLLECT("收藏"),
    CANCEL_COLLECT("取消收藏"),
    COPY("复制"),
    DETAILS("详情"),
    RENAME("重命名")
}

enum class SettingMenu(val value: String) {
    LICENSES("开放源代码许可"),
    DU_PRIVACY_AGREEMENT("小度隐私协议")
}
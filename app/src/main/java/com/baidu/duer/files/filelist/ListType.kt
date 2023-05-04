package com.baidu.duer.files.filelist

import androidx.annotation.IntDef

/**
 * @Author : 何飘
 * @CreateTime : 2023/1/30
 * @Description :
 */
@IntDef(
    ListType.HORIZONTAL,
    ListType.VERTICAL,
    ListType.RECENT,
    ListType.NONE,
    ListType.TITLE,
    ListType.BOTTOM_NOTICE
)

@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class ListType {
    companion object {
        const val NONE = 10000

        // 水平网格布局
        const val HORIZONTAL = 10001

        // 垂直布局
        const val VERTICAL = 10002

        // 最近更新布局
        const val RECENT = 10003

        // "最近更新"分栏标题holder
        const val TITLE = 10004

        // "最近更新"底部提醒内容holder
        const val BOTTOM_NOTICE = 10005
    }
}
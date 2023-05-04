package com.baidu.duer.files.message

import androidx.annotation.StringDef

/**
 * @Author : 何飘
 * @CreateTime : 2023/4/23
 * @Description :
 */
@StringDef(
    OperateType.NONE,
    OperateType.COPY,
    OperateType.MOVE,
    OperateType.DELETE,
    OperateType.COMPRESS,
    OperateType.UNZIP,
    OperateType.CANCEL_COPY,
    OperateType.CANCEL_MOVE,
    OperateType.CANCEL_DELETE,
    OperateType.CANCEL_COMPRESS,
    OperateType.CANCEL_UNZIP
)

@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class OperateType {
    companion object {
        const val NONE = "NONE"

        // 复制
        const val COPY = "COPY"

        // 移动
        const val MOVE = "MOVE"

        // 删除
        const val DELETE = "DELETE"

        // 压缩
        const val COMPRESS = "COMPRESS"

        // 解压
        const val UNZIP = "UNZIP"

        // 取消复制
        const val CANCEL_COPY = "CANCEL_COPY"

        // 取消移动
        const val CANCEL_MOVE = "CANCEL_MOVE"

        // 取消删除
        const val CANCEL_DELETE = "CANCEL_DELETE"

        // 取消压缩
        const val CANCEL_COMPRESS = "CANCEL_COMPRESS"

        // 取消解压
        const val CANCEL_UNZIP = "CANCEL_UNZIP"
    }
}
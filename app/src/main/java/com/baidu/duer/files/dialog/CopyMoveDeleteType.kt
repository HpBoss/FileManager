package com.baidu.duer.files.dialog

import androidx.annotation.AnyRes

/**
 * @Author : 何飘
 * @CreateTime : 2023/4/20
 * @Description :
 */
enum class CopyMoveDeleteType {
    COPY,
    MOVE,
    DELETE
}

fun CopyMoveDeleteType.getResourceId(
    @AnyRes copyRes: Int,
    @AnyRes moveRes: Int,
    @AnyRes deleteRes: Int
): Int =
    when (this) {
        CopyMoveDeleteType.COPY -> copyRes
        CopyMoveDeleteType.MOVE -> moveRes
        CopyMoveDeleteType.DELETE -> deleteRes
    }
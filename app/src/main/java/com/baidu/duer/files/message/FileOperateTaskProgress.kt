package com.baidu.duer.files.message

import androidx.annotation.StringDef

/**
 * @Author : 何飘
 * @CreateTime : 2023/3/3
 * @Description :
 */
@StringDef(
    FileOperateTaskProgress.NONE,
    FileOperateTaskProgress.START,
    FileOperateTaskProgress.ONGOING,
    FileOperateTaskProgress.FINISH,
)

@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class FileOperateTaskProgress {
    companion object {
        const val NONE = "NONE"

        // 开始
        const val START = "START"

        //
        const val ONGOING = "ONGOING"

        // 完成
        const val FINISH = "FINISH"
    }
}
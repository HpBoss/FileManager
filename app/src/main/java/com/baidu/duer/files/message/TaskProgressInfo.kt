package com.baidu.duer.files.message

import com.jeremyliao.liveeventbus.core.LiveEvent
import java8.nio.file.Path

/**
 * @Author : 何飘
 * @CreateTime : 2023/3/3
 * @Description :
 */
data class TaskProgressInfo(
    var progress: Long,
    val total: Long,
    @FileOperateTaskProgress var progressType: String,
    @OperateType var taskType: String,
    var targetPath: Path? = null
) : LiveEvent {
    override fun toString(): String {
        return "TaskProgressInfo(progress=$progress, total=$total, type=$progressType, " +
                "taskType: $taskType, targetPath: $targetPath)"
    }
}

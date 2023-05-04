package com.baidu.duer.files.message

import com.jeremyliao.liveeventbus.core.LiveEvent

/**
 * @Author : 何飘
 * @CreateTime : 2023/4/20
 * @Description :
 */
data class FileException(val message: String) : LiveEvent {
    override fun toString(): String {
        return "FileException(message='$message')"
    }
}

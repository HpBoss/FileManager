package com.baidu.duer.files.message

import com.jeremyliao.liveeventbus.core.LiveEvent

/**
 * @Author : 何飘
 * @CreateTime : 2023/4/12
 * @Description :
 */
data class TargetFileName(var fileName: String) : LiveEvent {
    override fun toString(): String {
        return "TargetFileName(fileName='$fileName')"
    }
}
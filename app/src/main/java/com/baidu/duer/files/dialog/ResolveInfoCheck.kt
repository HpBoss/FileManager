package com.baidu.duer.files.dialog

import android.content.pm.ResolveInfo

/**
 * @Author : 何飘
 * @CreateTime : 2023/3/8
 * @Description :
 */
data class ResolveInfoCheck(val resolveInfo: ResolveInfo?, var isChecked: Boolean = false)

package com.baidu.duer.files.util

import com.baidu.duer.files.app.application

/**
 * @Author : 何飘
 * @CreateTime : 2023/2/6
 * @Description :
 */

val Float.px: Float
    get() {
        val scale = application.resources.displayMetrics.density
        return this * scale + 0.5f
    }

val Float.dp: Float
    get() {
        val scale = application.resources.displayMetrics.density
        return this / scale + 0.5f
    }

val Int.px: Int
    get() {
        val scale = application.resources.displayMetrics.density
        return (this * scale + 0.5f).toInt()
    }

val Int.dp: Int
    get() {
        val scale = application.resources.displayMetrics.density
        return (this / scale + 0.5f).toInt()
    }
val Int.sp: Int get() = (this * application.resources.displayMetrics.scaledDensity + 0.5F).toInt()

val Float.sp: Float get() = this * application.resources.displayMetrics.scaledDensity + 0.5F
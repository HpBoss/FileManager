package com.baidu.duer.files.filelist

import androidx.annotation.IntDef

/**
 * @Author : 何飘
 * @CreateTime : 2023/2/9
 * @Description :
 */
@IntDef(
    TabType.NONE,
    TabType.RECENT,
    TabType.ALL,
    TabType.COLLECT,
    TabType.PICTURE,
    TabType.VIDEO,
    TabType.AUDIO,
    TabType.DOCUMENT,
    TabType.COMPRESS,
    TabType.APK,
    TabType.QQ,
    TabType.WECHAT
)

@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class TabType {
    companion object {
        const val NONE = 1000

        // 最近更新
        const val RECENT = 1001

        // 全部文件
        const val ALL = 1002

        // 我的收藏
        const val COLLECT = 1003

        // 图片
        const val PICTURE = 1004

        // 视频
        const val VIDEO = 1005

        // 音频
        const val AUDIO = 1006

        // 文档
        const val DOCUMENT = 1007

        // 压缩包
        const val COMPRESS = 1008

        // 安装包
        const val APK = 1009

        // QQ
        const val QQ = 1010

        // 微信
        const val WECHAT = 1011
    }
}
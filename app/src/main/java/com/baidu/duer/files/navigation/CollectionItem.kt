package com.baidu.duer.files.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

/**
 * @Author : 何飘
 * @CreateTime : 2023/2/15
 * @Description :
 */
class CollectionItem(
    @DrawableRes val iconCollect: Int?,
    @StringRes val titleCollect: Int,
    tabType: Int
) : AssortItem(iconCollect, titleCollect, tabType)
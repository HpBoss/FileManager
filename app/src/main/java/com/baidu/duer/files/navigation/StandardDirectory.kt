package com.baidu.duer.files.navigation

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class StandardDirectory internal constructor(
    @DrawableRes val iconRes: Int,
    @StringRes private val titleRes: Int,
    private val customTitle: String?,
    val tabType: Int,
    val relativePath: String,
    val isEnabled: Boolean
) {
    constructor(
        @DrawableRes iconRes: Int,
        @StringRes titleRes: Int,
        tabType: Int,
        relativePath: String,
        enabled: Boolean
    ) : this(iconRes, titleRes, null, tabType, relativePath, enabled)

    val id: Long
        get() = relativePath.hashCode().toLong()

    val key: String
        get() = relativePath

    fun getTitle(context: Context): String =
        if (!customTitle.isNullOrEmpty()) customTitle else context.getString(titleRes)

    fun withSettings(settings: StandardDirectorySettings): StandardDirectory =
        StandardDirectory(
            iconRes,
            titleRes,
            settings.customTitle,
            tabType,
            relativePath,
            settings.isEnabled
        )

    fun toSettings(): StandardDirectorySettings =
        StandardDirectorySettings(relativePath, customTitle, isEnabled)
}

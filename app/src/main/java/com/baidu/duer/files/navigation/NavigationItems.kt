package com.baidu.duer.files.navigation

import android.os.Environment
import androidx.annotation.Size
import com.baidu.duer.files.R
import com.baidu.duer.files.filelist.TabType
import com.baidu.duer.files.settings.Settings
import com.baidu.duer.files.util.*

/**
 * @Author : 何飘
 * @CreateTime : 2023/2/15
 * @Description :
 */
val navigationItems: List<NavigationItem?>
    get() =
        mutableListOf<NavigationItem?>().apply {
            add(RecentFileItem())
            addAll(storageItems)
            add(collectItem)
            addAll(assortItems)
            addAll(standardDirectoryItems)
        }

private val storageItems: List<NavigationItem>
    @Size(min = 0)
    get() = Settings.STORAGES.valueCompat.filter { it.isVisible }.map { StorageItem(it) }

private val standardDirectoryItems: List<NavigationItem>
    @Size(min = 0)
    get() =
        StandardDirectoriesLiveData.valueCompat
            .filter { it.isEnabled }
            .map { StandardDirectoryItem(it) }

val standardDirectories: List<StandardDirectory>
    get() {
        val settingsMap = Settings.STANDARD_DIRECTORY_SETTINGS.valueCompat.associateBy { it.id }
        return defaultStandardDirectories.map {
            val settings = settingsMap[it.key]
            if (settings != null) it.withSettings(settings) else it
        }
    }

private const val relativePathSeparator = ":"

private val defaultStandardDirectories: List<StandardDirectory>
    // 存在QQ、WeChat目录时才进行ICON展示
    get() =
        DEFAULT_STANDARD_DIRECTORIES.mapNotNull {
            when (it.iconRes) {
                R.drawable.qq_icon, R.drawable.wechat_icon -> {
                    for (relativePath in it.relativePath.split(relativePathSeparator)) {
                        return@mapNotNull it.copy(relativePath = relativePath)
                    }
                    null
                }
                else -> it
            }
        }

// @see android.os.Environment#STANDARD_DIRECTORIES
val DEFAULT_STANDARD_DIRECTORIES = listOf(
    StandardDirectory(
        R.drawable.qq_icon, R.string.navigation_standard_directory_qq, TabType.QQ,
        listOf(
            "Android/data/com.tencent.mobileqq/Tencent/QQfile_recv",
            "Tencent/QQ_Images",
            "Tencent/QQ_Video",
            "Pictures/QQ",
            "Download/QQ"
        )
            .joinToString(relativePathSeparator), true
    ),
    StandardDirectory(
        R.drawable.wechat_icon, R.string.navigation_standard_directory_wechat, TabType.WECHAT,
        listOf(
            "Android/data/com.tencent.mm/MicroMsg/Download",
            "Tencent/MicroMsg/Download",
            "Pictures/WeiXin",
            "Download/WeiXin",
            "DCIM/WeiXin"
        )
            .joinToString(relativePathSeparator), true
    )
)

internal fun getExternalStorageDirectory(relativePath: String): String =
    @Suppress("DEPRECATION")
    Environment.getExternalStoragePublicDirectory(relativePath).path

private val bookmarkDirectoryItems: List<NavigationItem>
    @Size(min = 0)
    get() = Settings.BOOKMARK_DIRECTORIES.valueCompat.map { BookmarkDirectoryItem(it) }

private val assortItems: List<NavigationItem>
    get() = listOf(
        AssortItem(
            R.drawable.picture_icon,
            R.string.navigation_standard_directory_pictures,
            TabType.PICTURE
        ),
        AssortItem(
            R.drawable.video_icon,
            R.string.navigation_assort_directory_videos,
            TabType.VIDEO
        ),
        AssortItem(
            R.drawable.audio_icon,
            R.string.navigation_assort_directory_audios,
            TabType.AUDIO
        ),
        AssortItem(
            R.drawable.document_icon,
            R.string.navigation_assort_directory_documents,
            TabType.DOCUMENT
        ),
        AssortItem(
            R.drawable.compress_icon,
            R.string.navigation_assort_directory_compress,
            TabType.COMPRESS
        ),
        AssortItem(R.drawable.apk_icon, R.string.navigation_assort_directory_apks, TabType.APK)
    )

private val collectItem by lazy {
    CollectionItem(null, R.string.collection_title, TabType.COLLECT)
}

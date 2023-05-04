package com.baidu.duer.files.navigation

import android.annotation.SuppressLint
import android.app.usage.StorageStatsManager
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.database.Cursor
import android.graphics.Typeface
import android.net.Uri
import android.os.Environment
import android.os.storage.StorageManager
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import com.baidu.duer.files.R
import com.baidu.duer.files.app.application
import com.baidu.duer.files.app.packageManager
import com.baidu.duer.files.file.*
import com.baidu.duer.files.filejob.CopyFileJob
import com.baidu.duer.files.filelist.FileListLiveData
import com.baidu.duer.files.message.FileException
import com.baidu.duer.files.message.TaskProgressInfo
import com.baidu.duer.files.provider.common.*
import com.baidu.duer.files.provider.common.exists
import com.baidu.duer.files.util.*
import com.jeremyliao.liveeventbus.LiveEventBus
import java8.nio.file.LinkOption
import java8.nio.file.Path
import java8.nio.file.Paths
import java.io.File
import java.util.*

/**
 * @Author : 何飘
 * @CreateTime : 2023/2/15
 * @Description :
 */

private val filterPackageName by lazy {
    arrayListOf(
        "com.alipay.arome.app",
        "com.android.htmlviewer"
    )
}

@SuppressLint("NewApi")
fun getMainStorageStats(context: Context): String {
    val externalDirs = context.getExternalFilesDirs(null)
    val storageManager =
        context.getSystemService(AppCompatActivity.STORAGE_SERVICE) as StorageManager
    var totalSpaceString = ""
    var freeSpaceString = ""
    var useSpaceString = ""
    externalDirs.forEach { file ->
        val storageVolume = storageManager.getStorageVolume(file) ?: return ""
        if (storageVolume.isPrimary) {
            // internal storage
            val storageStatsManager =
                context.getSystemService(AppCompatActivity.STORAGE_STATS_SERVICE) as StorageStatsManager
            val uuid = StorageManager.UUID_DEFAULT
            val totalSpace = storageStatsManager.getTotalBytes(uuid)
            val freeSpace = storageStatsManager.getFreeBytes(uuid)
            val useSpace = totalSpace - freeSpace
            // 总空间保留整数，剩余空间保留小数
            totalSpaceString = totalSpace.asFileSize().formatHumanShortReadable(context)
            freeSpaceString = freeSpace.asFileSize().formatHumanReadable(context)
            useSpaceString = useSpace.asFileSize().formatHumanReadable(context)
        } else {
            // sd card
            val totalSpace = file.totalSpace
            val freeSpace = file.freeSpace
            val useSpace = totalSpace - freeSpace
        }
    }
    return context.getString(
        R.string.navigation_storage_subtitle_format, useSpaceString, totalSpaceString
    )
}

fun getMediaStoreDirectory(uri: Uri): ArrayList<FileItem> {
    val listItems = arrayListOf<FileItem>()
    val projection = arrayOf(
        MediaStore.Files.FileColumns.MIME_TYPE,
        MediaStore.Files.FileColumns.DATA,
        MediaStore.Files.FileColumns.DISPLAY_NAME,
        MediaStore.Files.FileColumns.DATE_MODIFIED
    )
    application.queryCursor(uri, projection) { cursor ->
        try {
            val path = cursor.getStringValue(MediaStore.Files.FileColumns.DATA)
            if (path.contains(
                    Environment.getExternalStorageDirectory().absolutePath + "/Android"
                )
            ) return@queryCursor
            if (Paths.get(path).exists()) listItems.add(Paths.get(path).loadFileItem())
        } catch (e: Exception) {
            Log.e("getMediaStoreDirectory", "${e.message}")
        }
    }
    return listItems
}


@SuppressLint("Range")
fun getCompressResources(): ArrayList<FileItem> {
    val listItems = arrayListOf<FileItem>()
    val extensions = arrayOf(".zip", ".rar", ".tar", ".gz", ".7z", ".tar.xz")
    val resolver: ContentResolver = application.contentResolver
    val filesUri = MediaStore.Files.getContentUri("external")

    var selectionClause =
        MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_NONE + " AND ("
    for (i in extensions.indices) {
        if (i != 0) {
            selectionClause += " OR "
        }
        selectionClause += MediaStore.Files.FileColumns.DATA + " LIKE ?"
    }
    selectionClause += ")"
    val selectionArgsArray = arrayOfNulls<String>(extensions.size)
    for (i in extensions.indices) {
        selectionArgsArray[i] = "%" + extensions[i]
    }

    val filesCursor = resolver.query(
        filesUri,
        arrayOf(MediaStore.Files.FileColumns.DATA),
        selectionClause,
        selectionArgsArray,
        null
    )

    while (filesCursor != null && filesCursor.moveToNext()) {
        val path =
            filesCursor.getString(filesCursor.getColumnIndex(MediaStore.Files.FileColumns.DATA))
        if (path.contains(Environment.getExternalStorageDirectory().absolutePath + "/Android")) continue
        try {
            if (Paths.get(path).exists()) listItems.add(Paths.get(path).loadFileItem())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    filesCursor?.close()
    return listItems
}

fun getMimeTypeResources(
    resourcesNameArray: ArrayList<String>,
    isQueryDataDirectory: Boolean = false
): ArrayList<FileItem> {
    val listItems = arrayListOf<FileItem>()

    val uri = MediaStore.Files.getContentUri("external")
    val projection = arrayOf(
        MediaStore.Files.FileColumns.MIME_TYPE,
        MediaStore.Files.FileColumns.DATA,
        MediaStore.Files.FileColumns.DISPLAY_NAME,
        MediaStore.Files.FileColumns.SIZE
    )
    application.queryCursor(uri, projection) { cursor ->
        try {
            val fullMimetype =
                cursor.getStringValue(MediaStore.Files.FileColumns.MIME_TYPE)
                    ?.lowercase(
                        Locale.getDefault()
                    ) ?: return@queryCursor

            if (!resourcesNameArray.contains(fullMimetype.substringBefore(DEFAULT_PATH))
                && !resourcesNameArray.contains(fullMimetype.substringAfter(DEFAULT_PATH))
                && !resourcesNameArray.contains(fullMimetype)
            ) return@queryCursor

            val path = cursor.getStringValue(MediaStore.Files.FileColumns.DATA)
            if (File(path).isDirectory || (!isQueryDataDirectory && path.contains(
                    Environment.getExternalStorageDirectory().absolutePath + "/Android"
                ))
            ) return@queryCursor

            if (Paths.get(path).exists()) listItems.add(Paths.get(path).loadFileItem())
        } catch (e: Exception) {
            Log.e("getMimeTypeResources", "${e.message}")
        }
    }

    return listItems
}

fun getOpenIntentActivities(uri: Uri): List<ResolveInfo?> {
    // 获取扩展名
    val extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
    // 获取MimeType
    val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    // 创建隐式Intent
    val intent = Intent(Intent.ACTION_VIEW)
    intent.setDataAndType(uri, mimeType)
    // 根据Intent查询匹配的Activity列表
    return packageManager.queryIntentActivities(intent, PackageManager.MATCH_DIRECT_BOOT_UNAWARE)
        .filter { !filterPackageName.contains(it.activityInfo.packageName) && it.activityInfo.exported }
}

fun getShareApps(
    uris: ArrayList<Uri>,
    mimeTypes: Collection<MimeType>
): List<ResolveInfo?> {
    // 有些应用不支持多文件分享，在此需要根据选中的文件个数，选择分享Intent Action
    val queryAction = if (uris.size > 1) Intent.ACTION_SEND_MULTIPLE else Intent.ACTION_SEND
    val intent = Intent(queryAction)
        .addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        .setType(mimeTypes.intentType)
        .putExtra(Intent.EXTRA_STREAM, uris)
    return packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        .filter { !filterPackageName.contains(it.activityInfo.packageName) && it.activityInfo.exported }
}

fun createFromAsset(): Typeface {
    return Typeface.create("sans-serif-medium", Typeface.NORMAL)
}

fun sendFileOperateProgress(taskProgressInfo: TaskProgressInfo) {
    Log.i("sendFileOperateProgress", "taskProgressInfo: $taskProgressInfo")
    LiveEventBus
        .get(TaskProgressInfo::class.java)
        .post(taskProgressInfo)
}

fun sendDelayFileOperateProgress(taskProgressInfo: TaskProgressInfo, delay: Long) {
    Log.i("sendDelayFileOperateProgress", "taskProgressInfo: $taskProgressInfo, delay: $delay")
    LiveEventBus
        .get(TaskProgressInfo::class.java)
        .postDelay(taskProgressInfo, delay)
}

fun receiveFileOperateProgress(
    lifecycleOwner: LifecycleOwner,
    onChange: (compressType: TaskProgressInfo) -> Unit
) {
    LiveEventBus
        .get(TaskProgressInfo::class.java)
        .observe(
            lifecycleOwner
        ) {
            onChange(it)
        }
}

fun sendFileExceptionMessage(message: String) {
    LiveEventBus.get(FileException::class.java)
        .post(FileException(message))
}

fun receiveFileExceptionMessage(
    lifecycleOwner: LifecycleOwner,
    onChange: (fileException: FileException) -> Unit
) {
    LiveEventBus.get(FileException::class.java)
        .observe(lifecycleOwner) {
            onChange(it)
        }
}

fun getTargetPathForDuplicate(source: Path, isDirectory: Boolean): Path {
    source.asByteStringListPath()
    val sourceFileName = source.fileNameByteString!!
    val countEndIndex = if (isDirectory) {
        sourceFileName.length
    } else {
        sourceFileName.asFileName().baseName.length
    }
    val countInfo = getDuplicateCountInfo(sourceFileName, countEndIndex)
    var i = countInfo.count
    while (i >= 0) {
        val targetFileName = setDuplicateCount(sourceFileName, countInfo, i)
        val target = source.resolveSibling(targetFileName)
        if (!target.exists(LinkOption.NOFOLLOW_LINKS)) {
            return target
        }
        ++i
    }
    return source
}

fun getDuplicateCountInfo(fileName: ByteString, countEnd: Int): CopyFileJob.DuplicateCountInfo {
    while (true) {
        // /(?<=.) \(\d+\)$/
        var index = countEnd - 1
        // \)
        if (index < 0 || fileName[index] != ')'.code.toByte()) {
            break
        }
        --index
        // \d+
        val digitsEndInclusive = index
        while (index >= 0) {
            val b = fileName[index]
            if (b < '0'.code.toByte() || b > '9'.code.toByte()) {
                break
            }
            --index
        }
        if (index == digitsEndInclusive) {
            break
        }
        val countString = fileName.substring(index + 1, digitsEndInclusive + 1).toString()
        val count = try {
            countString.toInt()
        } catch (e: NumberFormatException) {
            break
        }
        // \(
        if (index < 0 || fileName[index] != '('.code.toByte()) {
            break
        }
        --index
        //
        if (index < 0 || fileName[index] != ' '.code.toByte()) {
            break
        }
        // (?<=.)
        if (index == 0) {
            break
        }
        return CopyFileJob.DuplicateCountInfo(index, countEnd, count)
    }
    return CopyFileJob.DuplicateCountInfo(countEnd, countEnd, 0)
}

fun setDuplicateCount(
    fileName: ByteString,
    countInfo: CopyFileJob.DuplicateCountInfo,
    count: Int
): ByteString {
    val fileSuffix = if (count == 0) "" else " ($count)"
    return ByteStringBuilder(fileName.substring(0, countInfo.countStart))
        .append(fileSuffix.toByteString())
        .append(fileName.substring(countInfo.countEnd))
        .toByteString()
}

fun separateQQWeXinFilePath(tabType: Int): ArrayList<Path> {
    val resultPath = arrayListOf<Path>()
    DEFAULT_STANDARD_DIRECTORIES.map {
        if (it.tabType == tabType) {
            for (relativePath in it.relativePath.split(":")) {
                val pathString = Environment.getExternalStoragePublicDirectory(relativePath).path
                if (JavaFile.isDirectory(pathString)) {
                    resultPath.add(Paths.get(pathString))
                }
            }
        }
    }
    return resultPath
}

fun Context.queryCursor(
    uri: Uri,
    projection: Array<String>,
    selection: String? = null,
    selectionArgs: Array<String>? = null,
    sortOrder: String? = null,
    callback: (cursor: Cursor) -> Unit
) {
    try {
        val cursor = contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)
        cursor?.use {
            if (cursor.moveToFirst()) {
                do {
                    callback(cursor)
                } while (cursor.moveToNext())
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun querySameFileName(
    owner: LifecycleOwner,
    path: Path,
    executeTask: (value: Stateful<List<FileItem>>) -> Unit
) {
    val fileListLiveData = FileListLiveData(path)

    fileListLiveData.observe(owner) {
        if (it is Loading) return@observe
        executeTask(it)
    }
}

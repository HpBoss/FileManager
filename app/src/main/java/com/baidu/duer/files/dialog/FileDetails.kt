package com.baidu.duer.files.dialog

import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.text.format.DateUtils
import android.util.Size
import androidx.exifinterface.media.ExifInterface
import com.baidu.duer.files.R
import com.baidu.duer.files.file.FileItem
import com.baidu.duer.files.file.MimeType
import com.baidu.duer.files.file.fileSize
import com.baidu.duer.files.file.formatLong
import com.baidu.duer.files.filelist.getMimeTypeName
import com.baidu.duer.files.fileproperties.date
import com.baidu.duer.files.fileproperties.extractMetadataNotBlank
import com.baidu.duer.files.fileproperties.image.*
import com.baidu.duer.files.fileproperties.location
import com.baidu.duer.files.fileproperties.video.VideoInfo
import com.baidu.duer.files.provider.common.getLastModifiedTime
import com.baidu.duer.files.provider.common.newInputStream
import com.baidu.duer.files.util.setDataSource
import com.caverock.androidsvg.SVG
import java8.nio.file.Path
import okio.buffer
import okio.source
import org.threeten.bp.Duration
import kotlin.math.roundToInt

/**
 * @Author : 何飘
 * @CreateTime : 2023/3/4
 * @Description :
 */
fun FileItem.path() = path.toString()

fun FileItem.lastModifiedTime() = attributes.lastModifiedTime().toInstant().formatLong()

// 文件夹的内容
fun FileItem.fileDescription(context: Context) = if (attributes.isDirectory) {
    val fileList = path.toFile().listFiles()
    var size = fileList?.size
    size?.let {
        fileList?.forEach {
            if (it.name.startsWith(".")) {
                size = size?.minus(1)
            }
        }
    }
    context.getString(R.string.folder_description, size)
} else {
    attributes.fileSize.formatHumanReadable(context)
}

fun FileItem.fileSize(context: Context) = attributes.fileSize.formatHumanReadable(context)

fun FileItem.createDate() = attributes.creationTime().toInstant().formatLong()

fun ImageInfo.resolutionRatio(context: Context) = if (dimensions != null) {
    context.getString(
        R.string.file_properties_media_dimensions_format,
        dimensions.width, dimensions.height
    )
} else {
    context.getString(R.string.unknown)
}

fun VideoInfo.resolutionRatio(context: Context) =
    context.getString(
        R.string.file_properties_media_dimensions_format,
        dimensions?.width, dimensions?.height
    )

fun FileItem.getTypeText(context: Context): String {
    val typeFormatRes = if (attributesNoFollowLinks.isSymbolicLink
        && !isSymbolicLinkBroken
    ) {
        R.string.file_properties_basic_type_symbolic_link_format
    } else {
        R.string.file_properties_basic_type_format
    }
    return context.getString(typeFormatRes, getMimeTypeName(context), mimeType.value)
}

// 图片
fun FileItem.imageInfoList(context: Context) = arrayListOf(
    FileInfo("路径", path()),
    FileInfo("类别", getTypeText(context)),
    FileInfo("分辨率", path.getImageInfo(mimeType).resolutionRatio(context)),
    FileInfo("大小", fileSize(context)),
    FileInfo("拍摄日期", createDate()),
)

// 视频
fun FileItem.videoInfoList(context: Context) = arrayListOf(
    FileInfo("路径", path()),
    FileInfo("类别", getTypeText(context)),
    FileInfo("时长", path.getVideoInfo().duration?.format().toString()),
    FileInfo("分辨率", path.getVideoInfo().resolutionRatio(context)),
    FileInfo("大小", fileSize(context)),
    FileInfo("拍摄日期", createDate()),
)

// 音频
fun FileItem.audioInfoList(context: Context) = arrayListOf(
    FileInfo("路径", path()),
    FileInfo("类别", getTypeText(context)),
    FileInfo("时长", path.getVideoInfo().duration?.format().toString()),
    FileInfo("大小", fileSize(context)),
    FileInfo("最后修改", lastModifiedTime()),
)

// 文件夹
fun FileItem.directoryInfoList(context: Context) = arrayListOf(
    FileInfo("路径", path()),
    FileInfo("类别", getTypeText(context)),
    FileInfo("内容", fileDescription(context)),
    FileInfo("最后修改", lastModifiedTime()),
)

// apk
fun FileItem.apkInfoList(context: Context) = arrayListOf(
    FileInfo("路径", path()),
    FileInfo("类别", getTypeText(context)),
    FileInfo("大小", fileSize(context)),
    FileInfo("最后修改", lastModifiedTime()),
)

// 其它文件
fun FileItem.fileInfoList(context: Context) = arrayListOf(
    FileInfo("路径", path()),
    FileInfo("类别", getTypeText(context)),
    FileInfo("大小", fileSize(context)),
    FileInfo("最后修改", lastModifiedTime()),
)

fun Path.getImageInfo(mimeType: MimeType) =
    when (mimeType) {
        MimeType.IMAGE_SVG_XML -> {
            val svg = newInputStream()
                // It seems we need Okio for SVG parser to work for files with entities.
                // Something weird is going on with buffering and mark/reset.
                //.buffer()
                //.use { SVG.getFromInputStream(it) }
                .source()
                .buffer()
                .use { SVG.getFromInputStream(it.inputStream()) }
            val width = svg.documentWidth
            val height = svg.documentHeight
            val dimensions = if (width != -1f && height != -1f) {
                Size(width.roundToInt(), height.roundToInt())
            } else {
                val viewBox = svg.documentViewBox
                if (viewBox != null) {
                    Size(viewBox.width().roundToInt(), viewBox.height().roundToInt())
                } else {
                    null
                }
            }
            ImageInfo(dimensions, null)
        }
        else -> {
            val bitmapOptions = BitmapFactory.Options()
                .apply { inJustDecodeBounds = true }
            newInputStream()
                .buffered()
                .use { BitmapFactory.decodeStream(it, null, bitmapOptions) }
            val width = bitmapOptions.outWidth
            val height = bitmapOptions.outHeight
            val dimensions = if (width != -1 && height != -1) {
                Size(width, height)
            } else {
                null
            }
            val exifInfo = try {
                val lastModifiedTime = getLastModifiedTime().toInstant()
                newInputStream().buffered().use {
                    val exifInterface = ExifInterface(it)
                    val dateTimeOriginal =
                        exifInterface.inferDateTimeOriginal(lastModifiedTime)
                    val gpsCoordinates = exifInterface.latLong?.let { it[0] to it[1] }
                    val gpsAltitude = exifInterface.gpsAltitude
                    val make =
                        exifInterface.getAttributeNotBlank(ExifInterface.TAG_MAKE)
                    val model =
                        exifInterface.getAttributeNotBlank(ExifInterface.TAG_MODEL)
                    val fNumber = exifInterface.getAttributeDoubleOrNull(
                        ExifInterface.TAG_F_NUMBER
                    )
                    val shutterSpeedValue = exifInterface.getAttributeDoubleOrNull(
                        ExifInterface.TAG_SHUTTER_SPEED_VALUE
                    )
                    val focalLength = exifInterface.getAttributeDoubleOrNull(
                        ExifInterface.TAG_FOCAL_LENGTH
                    )
                    val photographicSensitivity = exifInterface.getAttributeIntOrNull(
                        ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY
                    )
                    val software =
                        exifInterface.getAttributeNotBlank(ExifInterface.TAG_SOFTWARE)
                    val description = exifInterface.getAttributeNotBlank(
                        ExifInterface.TAG_IMAGE_DESCRIPTION
                    ) ?: exifInterface.getAttributeNotBlank(
                        ExifInterface.TAG_USER_COMMENT
                    )
                    val artist =
                        exifInterface.getAttributeNotBlank(ExifInterface.TAG_ARTIST)
                    val copyright =
                        exifInterface.getAttributeNotBlank(ExifInterface.TAG_COPYRIGHT)
                    ExifInfo(
                        dateTimeOriginal, gpsCoordinates, gpsAltitude, make,
                        model, fNumber, shutterSpeedValue, focalLength,
                        photographicSensitivity, software, description, artist,
                        copyright
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
            ImageInfo(dimensions, exifInfo)
        }
    }

fun Path.getVideoInfo() =
    MediaMetadataRetriever().use { retriever ->
        retriever.setDataSource(this)
        val title = retriever.extractMetadataNotBlank(
            MediaMetadataRetriever.METADATA_KEY_TITLE
        )
        val width = retriever.extractMetadataNotBlank(
            MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH
        )?.toIntOrNull()
        val height = retriever.extractMetadataNotBlank(
            MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT
        )?.toIntOrNull()
        val dimensions = if (width != null && height != null) {
            Size(width, height)
        } else {
            null
        }
        val duration = retriever.extractMetadataNotBlank(
            MediaMetadataRetriever.METADATA_KEY_DURATION
        )?.toLongOrNull()?.let { Duration.ofMillis(it) }
        val date = retriever.date
        val location = retriever.location
        val bitRate = retriever.extractMetadataNotBlank(
            MediaMetadataRetriever.METADATA_KEY_BITRATE
        )?.toLongOrNull()
        VideoInfo(title, dimensions, duration, date, location, bitRate)
    }

fun Duration.format(): String = DateUtils.formatElapsedTime(seconds)
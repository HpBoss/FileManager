package com.baidu.duer.files.util

import com.baidu.duer.files.filelist.TabType

const val RECENT_LIMIT = 50

const val DEFAULT_PATH = "/"
const val MY_COLLECTION = ""
const val ALL_FILE = "/storage/emulated/0"

const val IMAGES = "images"
const val VIDEOS = "videos"
const val AUDIOS = "audios"
const val DOCUMENTS = "documents"
const val APKS = "apks"
const val OTHERS = "others"
const val ARCHIVES = "archives"
const val SHOW_MIMETYPE = "show_mimetype"

const val REQUEST_CODE = 2000
const val RESULT_CODE = 2001
const val SEARCH_OPEN_FILE_RESULT_CODE = 2002
const val PATH = "path"
const val SELECT_TYPE = "select_type"
const val PASTE = "paste"
const val CUT = "cut"
const val TITLE = "title"
const val PATH_DATA = "path_data"

val standardDirectoryList = arrayListOf("image", "audio", "video", "text")

// what else should we count as an audio except "audio/*" mimetype
val extraAudioMimeTypes = arrayListOf("application/ogg")

val extraDocumentMimeTypes = arrayListOf(
    "text/plain",
    "application/pdf",
    "application/msword",
    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    "application/javascript",
    "application/vnd.ms-powerpoint",
    "application/xslt+xml",
    "application/vnd.openxmlformats-officedocument.presentationml.presentation"
)

val archiveMimeTypes = arrayListOf(
    "application/zip",
    "application/octet-stream",
    "application/json",
    "application/x-tar",
    "application/x-rar-compressed",
    "application/x-zip-compressed",
    "application/x-7z-compressed",
    "application/x-compressed",
    "application/x-gzip",
    "application/java-archive",
    "multipart/x-zip",
    "application/x-xz"
)

val apkMimeType = "application/vnd.android.package-archive"

val tabNameMap = hashMapOf(TabType.RECENT to "最近更新")

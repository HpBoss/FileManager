package com.baidu.duer.files.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @Author : 何飘
 * @CreateTime : 2023/3/9
 * @Description :
 */
@Entity
data class Mime(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @ColumnInfo(name = "mime_type") var mimeType: String? = null,
    @ColumnInfo(name = "package_name") var packageName: String? = null,
    @ColumnInfo(name = "class_name") var className: String? = null
)
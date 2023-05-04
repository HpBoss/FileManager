package com.baidu.duer.files.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @Author : 何飘
 * @CreateTime : 2023/2/8
 * @Description :
 */
@Entity
data class Collect(
    // 想要id实现自增，id默认值只能写0
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @ColumnInfo(name = "path") var path: String? = null,
    @ColumnInfo(name = "path_name") var pathName: String? = null
)
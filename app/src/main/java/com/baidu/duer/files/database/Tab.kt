package com.baidu.duer.files.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @Author : 何飘
 * @CreateTime : 2023/2/7
 * @Description :
 */
@Entity
data class Tab(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @ColumnInfo(name = "type") var type: Int? = null,
    @ColumnInfo(name = "is_horizontal") var isHorizontal: Boolean? = true
)
package com.baidu.duer.files.database

import androidx.room.*

/**
 * @Author : 何飘
 * @CreateTime : 2023/2/8
 * @Description :
 */
@Dao
interface TabDao {
    @Query("SELECT * FROM tab WHERE type = :type")
    suspend fun getTab(type: Int): Tab?

    @Insert
    suspend fun insert(tab: Tab)

    @Delete
    suspend fun delete(tab: Tab)

    @Update
    suspend fun update(tab: Tab)
}
    
package com.baidu.duer.files.database

import androidx.room.*

/**
 * @Author : 何飘
 * @CreateTime : 2023/3/9
 * @Description :
 */
@Dao
interface MimeDao {
    @Query("SELECT * FROM mime WHERE mime_type = :mimeType")
    suspend fun getOpenApp(mimeType: String): Mime?

    @Query("SELECT * FROM mime")
    suspend fun getAllOpenApp(): MutableList<Mime>?

    @Insert
    suspend fun insert(vararg mime: Mime)

    @Query("DELETE FROM mime WHERE mime_type = :mimeType")
    suspend fun delete(mimeType: String)

    @Update
    suspend fun update(vararg mime: Mime)
}
    
package com.baidu.duer.files.database

import androidx.room.*

/**
 * @Author : 何飘
 * @CreateTime : 2023/2/8
 * @Description :
 */
@Dao
interface CollectDao {
    @Query("SELECT * FROM collect WHERE path = :path")
    suspend fun getCollect(path: String): Collect?

    @Query("SELECT * FROM collect")
    suspend fun getAllCollect(): MutableList<Collect>?

    @Insert
    suspend fun insert(vararg collect: Collect)

    @Query("DELETE FROM collect WHERE path = :path")
    suspend fun delete(path: String)

    @Query("UPDATE collect SET path = :newPath, path_name = :newPathName WHERE path = :path")
    suspend fun update(path: String, newPathName: String, newPath: String)
}
    
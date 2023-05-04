package com.baidu.duer.files.assort

import com.baidu.duer.files.app.application
import com.baidu.duer.files.database.AppDatabase
import com.baidu.duer.files.file.FileItem
import com.baidu.duer.files.file.loadFileItem
import com.baidu.duer.files.provider.common.exists
import java8.nio.file.Paths

/**
 * @Author : 何飘
 * @CreateTime : 2023/2/9
 * @Description :
 */
class FileCollectListLiveData : BaseFileAssortLiveData() {
    override suspend fun operateSuccessData(): ArrayList<FileItem> {
        val collectDao = AppDatabase.getDatabase(application)?.collectDao()
        val collectList = collectDao?.getAllCollect()
        // 当一个已经被收藏的文件或者文件夹，不经过我们的文件管理器删除后，我们的Collect数据是没有更新数据的
        // 因此在展示时需要判定当前得到的Paths是否存在文件
        collectList?.filter { !Paths.get(it.path).exists() }?.forEach {
            it.path?.let { path -> collectDao.delete(path) }
        }
        return collectList?.filter { Paths.get(it.path).exists() }
            ?.map { Paths.get(it.path).loadFileItem() } as ArrayList<FileItem>
    }
}

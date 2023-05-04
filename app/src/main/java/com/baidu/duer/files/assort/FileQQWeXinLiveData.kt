package com.baidu.duer.files.assort

import com.baidu.duer.files.file.FileItem
import com.baidu.duer.files.file.loadFileItem
import com.baidu.duer.files.provider.common.isRegularFile
import com.baidu.duer.files.provider.common.newDirectoryStream
import java8.nio.file.DirectoryIteratorException
import java8.nio.file.Path
import java.io.IOException

/**
 * @Author : 何飘
 * @CreateTime : 2023/3/30
 * @Description :
 */
class FileQQWeXinLiveData(private val pathList: List<Path>) : BaseFileAssortLiveData() {
    override suspend fun operateSuccessData(): ArrayList<FileItem> {
        val fileList = arrayListOf<FileItem>()
        pathList.forEach {
            it.newDirectoryStream().use { directoryStream ->
                for (path in directoryStream) {
                    try {
                        if (path.isRegularFile()) fileList.add(path.loadFileItem())
                    } catch (e: DirectoryIteratorException) {
                        e.printStackTrace()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
        return fileList
    }
}

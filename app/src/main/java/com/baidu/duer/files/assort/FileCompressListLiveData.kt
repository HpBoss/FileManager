package com.baidu.duer.files.assort

import com.baidu.duer.files.file.FileItem
import com.baidu.duer.files.navigation.getCompressResources


/**
 * @Author : 何飘
 * @CreateTime : 2023/2/16
 * @Description :
 */
class FileCompressListLiveData : BaseFileAssortLiveData() {
    override suspend fun operateSuccessData(): ArrayList<FileItem> = getCompressResources()
}

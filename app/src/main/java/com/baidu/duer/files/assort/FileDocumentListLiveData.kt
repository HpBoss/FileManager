package com.baidu.duer.files.assort

import com.baidu.duer.files.file.FileItem
import com.baidu.duer.files.navigation.getMimeTypeResources
import com.baidu.duer.files.util.extraDocumentMimeTypes

/**
 * @Author : 何飘
 * @CreateTime : 2023/2/16
 * @Description :
 */
class FileDocumentListLiveData : BaseFileAssortLiveData() {
    override suspend fun operateSuccessData(): ArrayList<FileItem> =
        getMimeTypeResources(extraDocumentMimeTypes)
}

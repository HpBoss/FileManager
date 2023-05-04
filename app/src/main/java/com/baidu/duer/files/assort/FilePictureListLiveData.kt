package com.baidu.duer.files.assort

import android.provider.MediaStore
import com.baidu.duer.files.file.FileItem
import com.baidu.duer.files.navigation.getMediaStoreDirectory

/**
 * @Author : 何飘
 * @CreateTime : 2023/2/16
 * @Description :
 */
class FilePictureListLiveData : BaseFileAssortLiveData() {
    override suspend fun operateSuccessData(): ArrayList<FileItem> =
        getMediaStoreDirectory(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
}

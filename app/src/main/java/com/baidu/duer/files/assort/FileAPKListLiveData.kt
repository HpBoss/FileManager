package com.baidu.duer.files.assort

import com.baidu.duer.files.file.FileItem
import com.baidu.duer.files.navigation.getMimeTypeResources
import com.baidu.duer.files.util.apkMimeType

/**
 * @Author : 何飘
 * @CreateTime : 2023/2/16
 * @Description : 如果后面的selectionSuffix需要拼接多个条件，使用sql语法书写即可，注意留空格，例如" LIKE '%%.apk' OR"
 * %%之间可以补充关键字，这里只需要获取该文件是.apk结尾的即可
 */

class FileAPKListLiveData : BaseFileAssortLiveData() {
    override suspend fun operateSuccessData(): ArrayList<FileItem> =
        getMimeTypeResources(arrayListOf(apkMimeType))

}

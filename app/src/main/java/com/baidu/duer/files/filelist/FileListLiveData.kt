package com.baidu.duer.files.filelist

import com.baidu.duer.files.file.FileItem
import com.baidu.duer.files.file.loadFileItem
import com.baidu.duer.files.provider.common.newDirectoryStream
import com.baidu.duer.files.util.*
import java8.nio.file.DirectoryIteratorException
import java8.nio.file.Path
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.IOException

class FileListLiveData(private val path: Path, isObserverPath: Boolean = true) :
    CloseableLiveData<Stateful<List<FileItem>>>() {
    private var observer: PathObserver? = null

    init {
        loadValue()
        // 路径选择页面不需要实时监听路径上的文件信息变化
        observer = if (isObserverPath) PathObserver(path) { onChangeObserved() } else null
    }

    override fun loadValue() {
        coroutineScope?.cancel()
        value = Loading(value?.value)
        coroutineScope = CoroutineScope(Dispatchers.IO)
        coroutineScope?.launch {
            val value = try {
                path.newDirectoryStream().use { directoryStream ->
                    val fileList = mutableListOf<FileItem>()
                    for (path in directoryStream) {
                        try {
                            fileList.add(path.loadFileItem())
                        } catch (e: DirectoryIteratorException) {
                            e.printStackTrace()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                    Success(fileList as List<FileItem>)
                }
            } catch (e: Exception) {
                Failure(valueCompat.value, e)
            }
            postValue(value)
        }
    }

    private fun onChangeObserved() {
        if (hasActiveObservers()) {
            loadValue()
        } else {
            isChangedWhileInactive = true
        }
    }

    override fun close() {
        observer?.close()
        super.close()
    }
}

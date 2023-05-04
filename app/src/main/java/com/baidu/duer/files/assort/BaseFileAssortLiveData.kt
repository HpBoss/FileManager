package com.baidu.duer.files.assort

import com.baidu.duer.files.file.FileItem
import com.baidu.duer.files.filelist.PathsObserver
import com.baidu.duer.files.util.*
import java8.nio.file.Path
import kotlinx.coroutines.*

/**
 * @Author : 何飘
 * @CreateTime : 2023/2/22
 * @Description :
 */
abstract class BaseFileAssortLiveData : CloseableLiveData<Stateful<List<FileItem>>>() {
    private var observer: PathsObserver? = null
    private var distinctPathList: List<Path> = listOf()

    init {
        loadValue()
    }

    abstract suspend fun operateSuccessData(): ArrayList<FileItem>

    final override fun loadValue() {
        coroutineScope?.cancel()
        value = Loading(value?.value)
        coroutineScope = CoroutineScope(Dispatchers.IO)
        coroutineScope?.launch {
            val value = try {
                val fileItems =
                    withContext(Dispatchers.Default) {
                        try {
                            withTimeout(3000) {
                                operateSuccessData()
                            }
                        } catch (e: TimeoutCancellationException) {
                            e.printStackTrace()
                            arrayListOf()
                        }
                    } as List<FileItem>
                distinctPathList =
                    distinctPathList.plus(fileItems.map { it.path.parent }.distinct()).distinct()
                observer?.close()
                observer = PathsObserver(distinctPathList) { onChangeObserved() }
                Success(fileItems)
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
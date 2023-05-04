package com.baidu.duer.files.filelist

import com.baidu.duer.files.file.FileItem
import com.baidu.duer.files.file.loadFileItem
import com.baidu.duer.files.provider.common.search
import com.baidu.duer.files.util.*
import java8.nio.file.Path
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.IOException

class SearchFileListLiveData(
    private val path: Path,
    private val query: String
) : CloseableLiveData<Stateful<List<FileItem>>>() {

    init {
        loadValue()
    }

    override fun loadValue() {
        coroutineScope?.cancel()
        value = Loading(emptyList())
        coroutineScope = CoroutineScope(Dispatchers.IO)
        coroutineScope?.launch {
            val fileList = mutableListOf<FileItem>()
            try {
                path.search(query, INTERVAL_MILLIS) { paths: List<Path> ->
                    for (path in paths) {
                        val fileItem = try {
                            path.loadFileItem()
                        } catch (e: IOException) {
                            e.printStackTrace()
                            // TODO: Support file without information.
                            continue
                        }
                        fileList.add(fileItem)
                    }
                    postValue(Loading(fileList.toList()))
                }
                postValue(Success(fileList))
            } catch (e: Exception) {
                // TODO: Retrieval of previous value is racy.
                postValue(Failure(valueCompat.value, e))
            }
        }
    }

    companion object {
        private const val INTERVAL_MILLIS = 500L
    }
}

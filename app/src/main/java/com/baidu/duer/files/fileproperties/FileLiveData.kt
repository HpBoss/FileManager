package com.baidu.duer.files.fileproperties

import android.os.AsyncTask
import com.baidu.duer.files.file.FileItem
import com.baidu.duer.files.file.loadFileItem
import com.baidu.duer.files.util.*
import java8.nio.file.Path

class FileLiveData private constructor(
    path: Path,
    file: FileItem?
) : PathObserverLiveData<Stateful<FileItem>>(path) {
    constructor(path: Path) : this(path, null)

    constructor(file: FileItem) : this(file.path, file)

    init {
        if (file != null) {
            value = Success(file)
        } else {
            loadValue()
        }
        observe()
    }

    override fun loadValue() {
        value = Loading(value?.value)
        AsyncTask.THREAD_POOL_EXECUTOR.execute {
            val value = try {
                val file = path.loadFileItem()
                Success(file)
            } catch (e: Exception) {
                Failure(valueCompat.value, e)
            }
            postValue(value)
        }
    }
}

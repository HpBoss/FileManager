package com.baidu.duer.files.selectfile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.baidu.duer.files.file.FileItem
import com.baidu.duer.files.filelist.FileListLiveData
import com.baidu.duer.files.util.CloseableLiveData
import com.baidu.duer.files.util.Stateful
import com.baidu.duer.files.util.valueCompat
import java8.nio.file.Path
import java.io.Closeable

/**
 * @Author : 何飘
 * @CreateTime : 2023/2/13
 * @Description :
 */
class MoveFileListSwitchMapLiveData(
    private val pathLiveData: LiveData<Path>
) : MediatorLiveData<Stateful<List<FileItem>>>(), Closeable {
    private var liveData: CloseableLiveData<Stateful<List<FileItem>>>? = null

    init {
        // pathLiveData发生变化，执行一遍updateSource
        addSource(pathLiveData) { updateSource() }
    }

    private fun updateSource() {
        liveData?.let {
            removeSource(it)
            it.close()
        }
        val path = pathLiveData.valueCompat
        val liveData = FileListLiveData(path, false)
        this.liveData = liveData
        addSource(liveData) { value = it }
    }

    fun reload() {
        when (val liveData = liveData) {
            is FileListLiveData -> liveData.loadValue()
        }
    }

    override fun close() {
        liveData?.let {
            removeSource(it)
            it.close()
            this.liveData = null
        }
    }
}
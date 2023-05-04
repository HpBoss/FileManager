package com.baidu.duer.files.assort

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.baidu.duer.files.file.FileItem
import com.baidu.duer.files.filelist.*
import com.baidu.duer.files.navigation.separateQQWeXinFilePath
import com.baidu.duer.files.util.CloseableLiveData
import com.baidu.duer.files.util.Stateful
import com.baidu.duer.files.util.valueCompat
import java8.nio.file.Path
import java.io.Closeable

/**
 * @Author : 何飘
 * @CreateTime : 2023/3/31
 * @Description :
 */
class FileListSwitchMapLiveData(
    private val pathLiveData: LiveData<Path>,
    private val tabTypeLiveData: LiveData<Int>,
    private val searchStateLiveData: LiveData<SearchState>,
    private val listLayoutStateLiveData: LiveData<ListLayoutData?>
) : MediatorLiveData<Stateful<List<FileItem>>>(), Closeable {
    private var liveData: CloseableLiveData<Stateful<List<FileItem>>>? = null

    init {
        // pathLiveData发生变化，执行一遍updateSource
        addSource(pathLiveData) { updateSource() }
        addSource(tabTypeLiveData) {
            if (tabTypeLiveData.valueCompat != TabType.ALL) updateSource()
        }
        addSource(searchStateLiveData) { updateSource() }
    }

    private fun updateSource() {
        liveData?.let {
            removeSource(it)
            it.close()
        }
        val path = pathLiveData.valueCompat
        val tabType = tabTypeLiveData.valueCompat
        val searchState = searchStateLiveData.valueCompat
        val listLayoutState = listLayoutStateLiveData.valueCompat
        val liveData = if (searchState.isSearching) {
            SearchFileListLiveData(path, searchState.query)
        } else if (tabType == TabType.RECENT) {
            FileRecentListLiveData()
        } else if (tabType == TabType.COLLECT && listLayoutState?.layout?.size == 1) {
            FileCollectListLiveData()
        } else if (tabType == TabType.PICTURE) {
            FilePictureListLiveData()
        } else if (tabType == TabType.VIDEO) {
            FileVideoListLiveData()
        } else if (tabType == TabType.AUDIO) {
            FileAudioListLiveData()
        } else if (tabType == TabType.DOCUMENT) {
            FileDocumentListLiveData()
        } else if (tabType == TabType.COMPRESS) {
            FileCompressListLiveData()
        } else if (tabType == TabType.APK) {
            FileAPKListLiveData()
        } else if (tabType == TabType.QQ || tabType == TabType.WECHAT) {
            FileQQWeXinLiveData(separateQQWeXinFilePath(tabType))
        } else {
            FileListLiveData(path)
        }
        this.liveData = liveData
        addSource(liveData) { value = it }
    }

    fun reload() {
        when (val liveData = liveData) {
            is FileListLiveData -> liveData.loadValue()
            is SearchFileListLiveData -> liveData.loadValue()
            is FileRecentListLiveData -> liveData.loadValue()
            is FileCollectListLiveData -> liveData.loadValue()
            is FilePictureListLiveData -> liveData.loadValue()
            is FileVideoListLiveData -> liveData.loadValue()
            is FileAudioListLiveData -> liveData.loadValue()
            is FileDocumentListLiveData -> liveData.loadValue()
            is FileCompressListLiveData -> liveData.loadValue()
            is FileQQWeXinLiveData -> liveData.loadValue()
            is FileAPKListLiveData -> liveData.loadValue()
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
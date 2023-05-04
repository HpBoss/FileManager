package com.baidu.duer.files.filelist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.baidu.duer.files.settings.PathSettings
import com.baidu.duer.files.settings.SettingLiveData
import com.baidu.duer.files.settings.Settings
import com.baidu.duer.files.util.valueCompat
import java8.nio.file.Path

class FileSortPathSpecificLiveData(pathLiveData: LiveData<Path>, tabTypeLiveData: LiveData<Int>) :
    MediatorLiveData<Boolean>() {
    private lateinit var pathSortOptionsLiveData: SettingLiveData<FileSortOptions?>

    private fun loadValue() {
        val value = pathSortOptionsLiveData.value != null
        if (this.value != value) {
            this.value = value
        }
    }

    fun putValue(value: Boolean) {
        if (value) {
            if (pathSortOptionsLiveData.value == null) {
                pathSortOptionsLiveData.putValue(Settings.FILE_LIST_SORT_OPTIONS.valueCompat)
            }
        } else {
            if (pathSortOptionsLiveData.value != null) {
                pathSortOptionsLiveData.putValue(null)
            }
        }
    }

    private fun addPathSortOptionsLiveDataSource(pathSortOptionsLiveData: SettingLiveData<FileSortOptions?>) {
        if (this::pathSortOptionsLiveData.isInitialized) {
            removeSource(pathSortOptionsLiveData)
        }
        this.pathSortOptionsLiveData = pathSortOptionsLiveData
        // 默认开启作用于当前文件夹的文件排序
        this.pathSortOptionsLiveData.value
            ?: this.pathSortOptionsLiveData.putValue(Settings.FILE_LIST_SORT_OPTIONS.valueCompat)
        addSource(this.pathSortOptionsLiveData) { loadValue() }
    }

    init {
        addSource(pathLiveData) { path: Path ->
            addPathSortOptionsLiveDataSource(PathSettings.getFileListSortOptions(path))
        }
        addSource(tabTypeLiveData) {
            if (tabTypeLiveData.valueCompat != TabType.ALL) {
                addPathSortOptionsLiveDataSource(PathSettings.getFileListSortOptions(tabTypeLiveData.valueCompat))
            }
        }
    }
}

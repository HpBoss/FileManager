package com.baidu.duer.files.filelist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.baidu.duer.files.filelist.FileSortOptions.By
import com.baidu.duer.files.filelist.FileSortOptions.Order
import com.baidu.duer.files.settings.PathSettings
import com.baidu.duer.files.settings.SettingLiveData
import com.baidu.duer.files.settings.Settings
import com.baidu.duer.files.util.valueCompat
import java8.nio.file.Path

class FileSortOptionsLiveData(
    pathLiveData: LiveData<Path>,
    tabTypeLiveData: LiveData<Int> = MutableLiveData()
) : MediatorLiveData<FileSortOptions>() {
    private lateinit var pathSortOptionsLiveData: SettingLiveData<FileSortOptions?>

    private fun loadValue() {
        if (!this::pathSortOptionsLiveData.isInitialized) {
            // Not yet initialized.
            return
        }
        val value = pathSortOptionsLiveData.value ?: Settings.FILE_LIST_SORT_OPTIONS.valueCompat
        if (this.value != value) {
            this.value = value
        }
    }

    fun putBy(by: By) {
        putValue(valueCompat.copy(by = by))
    }

    fun putOrder(order: Order) {
        putValue(valueCompat.copy(order = order))
    }

    fun putIsDirectoriesFirst(isDirectoriesFirst: Boolean) {
        putValue(valueCompat.copy(isDirectoriesFirst = isDirectoriesFirst))
    }

    private fun putValue(value: FileSortOptions) {
        if (pathSortOptionsLiveData.value != null) {
            pathSortOptionsLiveData.putValue(value)
        } else {
            Settings.FILE_LIST_SORT_OPTIONS.putValue(value)
        }
    }

    private fun addPathSortOptionsLiveDataSource(pathSortOptionsLiveData: SettingLiveData<FileSortOptions?>) {
        if (this::pathSortOptionsLiveData.isInitialized) {
            removeSource(pathSortOptionsLiveData)
        }
        this.pathSortOptionsLiveData = pathSortOptionsLiveData
        addSource(this.pathSortOptionsLiveData) { loadValue() }
    }

    init {
        addSource(Settings.FILE_LIST_SORT_OPTIONS) { loadValue() }
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

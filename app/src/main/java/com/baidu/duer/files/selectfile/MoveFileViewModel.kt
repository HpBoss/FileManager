package com.baidu.duer.files.selectfile

import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.baidu.duer.files.file.FileItem
import com.baidu.duer.files.filelist.*
import com.baidu.duer.files.util.Stateful
import com.baidu.duer.files.util.valueCompat
import java8.nio.file.Path

/**
 * @Author : 何飘
 * @CreateTime : 2023/2/13
 * @Description :
 */
class MoveFileViewModel : ViewModel() {
    private val trailLiveData = TrailLiveData()
    val currentPathLiveData = trailLiveData.map { it.currentPath }
    val breadcrumbLiveData: LiveData<BreadcrumbData> = BreadcrumbLiveData(trailLiveData)
    private val _fileListLiveData = MoveFileListSwitchMapLiveData(currentPathLiveData)
    val fileListLiveData: LiveData<Stateful<List<FileItem>>>
        get() = _fileListLiveData
    val fileListStateful: Stateful<List<FileItem>>
        get() = _fileListLiveData.valueCompat
    val pendingState: Parcelable?
        get() = trailLiveData.valueCompat.pendingState

    private val _sortOptionsLiveData = FileSortOptionsLiveData(currentPathLiveData)
    val sortOptionsLiveData: LiveData<FileSortOptions> = _sortOptionsLiveData
    val sortOptions: FileSortOptions
        get() = _sortOptionsLiveData.valueCompat

    fun resetTo(path: Path) = trailLiveData.resetTo(path)

    fun navigateTo(lastState: Parcelable, path: Path) {
        trailLiveData.navigateTo(lastState, path)
    }

    fun navigateUp(overrideBreadcrumb: Boolean): Boolean =
        if (!overrideBreadcrumb && breadcrumbLiveData.valueCompat.selectedIndex == 0) {
            false
        } else {
            trailLiveData.navigateUp()
        }
}
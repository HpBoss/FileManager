package com.baidu.duer.files.filelist

import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.baidu.duer.files.assort.FileListSwitchMapLiveData
import com.baidu.duer.files.file.FileItem
import com.baidu.duer.files.filelist.FileSortOptions.By
import com.baidu.duer.files.filelist.FileSortOptions.Order
import com.baidu.duer.files.provider.archive.archiveRefresh
import com.baidu.duer.files.provider.archive.isArchivePath
import com.baidu.duer.files.util.DEFAULT_PATH
import com.baidu.duer.files.util.Stateful
import com.baidu.duer.files.util.valueCompat
import java8.nio.file.Path
import java8.nio.file.Paths

class FileListViewModel : ViewModel() {
    private val trailLiveData = TrailLiveData()
    val hasTrail: Boolean
        get() = trailLiveData.value != null
    val pendingState: Parcelable?
        get() = trailLiveData.valueCompat.pendingState

    fun navigateTo(lastState: Parcelable, path: Path) = trailLiveData.navigateTo(lastState, path)

    fun resetTo(path: Path) = trailLiveData.resetTo(path)

    fun navigateUp(overrideBreadcrumb: Boolean): Boolean =
        if (!overrideBreadcrumb && breadcrumbLiveData.valueCompat.selectedIndex == 0) {
            false
        } else {
            trailLiveData.navigateUp()
        }

    val currentPathLiveData = trailLiveData.map { it.currentPath }
    val currentPath: Path
        get() = currentPathLiveData.value ?: Paths.get(DEFAULT_PATH)

    val currentTabTypeLiveData = MutableLiveData(TabType.NONE)
    val currentTabType: Int
        get() = currentTabTypeLiveData.valueCompat

    fun updateCurrentTabType(tabType: Int) {
        currentTabTypeLiveData.value = tabType
    }

    private val _searchStateLiveData = MutableLiveData(SearchState(false, ""))
    val searchStateLiveData: LiveData<SearchState> = _searchStateLiveData
    val searchState: SearchState
        get() = _searchStateLiveData.valueCompat

    private val _listLayoutStateLiveData = MutableLiveData<ListLayoutData?>()
    var listLayoutState: ListLayoutData?
        get() = _listLayoutStateLiveData.value
        set(value) {
            _listLayoutStateLiveData.value = value
        }

    fun search(query: String) {
        val searchState = _searchStateLiveData.valueCompat
        if (searchState.isSearching && searchState.query == query) {
            return
        }
        _searchStateLiveData.value = SearchState(true, query)
    }

    fun stopSearching() {
        val searchState = _searchStateLiveData.valueCompat
        if (!searchState.isSearching) {
            return
        }
        _searchStateLiveData.value = SearchState(false, "")
    }

    private val _fileListLiveData =
        FileListSwitchMapLiveData(
            currentPathLiveData,
            currentTabTypeLiveData,
            _searchStateLiveData,
            _listLayoutStateLiveData
        )
    val fileListLiveData: LiveData<Stateful<List<FileItem>>>
        get() = _fileListLiveData
    val fileListStateful: Stateful<List<FileItem>>
        get() = _fileListLiveData.valueCompat

    fun reload() {
        val path = currentPath
        if (path.isArchivePath) {
            path.archiveRefresh()
        }
        _fileListLiveData.reload()
    }

    val searchViewExpandedLiveData = MutableLiveData(false)
    var isSearchViewExpanded: Boolean
        get() = searchViewExpandedLiveData.valueCompat
        set(value) {
            if (searchViewExpandedLiveData.valueCompat == value) {
                return
            }
            searchViewExpandedLiveData.value = value
        }

    private val _searchViewQueryLiveData = MutableLiveData("")
    var searchViewQuery: String
        get() = _searchViewQueryLiveData.valueCompat
        set(value) {
            if (_searchViewQueryLiveData.valueCompat == value) {
                return
            }
            _searchViewQueryLiveData.value = value
        }

    val breadcrumbLiveData: LiveData<BreadcrumbData> =
        BreadcrumbLiveData(trailLiveData, currentTabTypeLiveData)
    val breadcrumb: BreadcrumbData?
        get() = breadcrumbLiveData.value

    private val _sortOptionsLiveData =
        FileSortOptionsLiveData(currentPathLiveData, currentTabTypeLiveData)
    val sortOptionsLiveData: LiveData<FileSortOptions> = _sortOptionsLiveData
    val sortOptions: FileSortOptions
        get() = _sortOptionsLiveData.valueCompat

    fun setSortBy(by: By) = _sortOptionsLiveData.putBy(by)

    fun setSortOrder(order: Order) = _sortOptionsLiveData.putOrder(order)

    fun setSortDirectoriesFirst(isDirectoriesFirst: Boolean) =
        _sortOptionsLiveData.putIsDirectoriesFirst(isDirectoriesFirst)

    private val _sortPathSpecificLiveData =
        FileSortPathSpecificLiveData(currentPathLiveData, currentTabTypeLiveData)
    val sortPathSpecificLiveData: LiveData<Boolean>
        get() = _sortPathSpecificLiveData
    var isSortPathSpecific: Boolean
        get() = _sortPathSpecificLiveData.valueCompat
        set(value) {
            _sortPathSpecificLiveData.putValue(value)
        }

    private val _pickOptionsLiveData = MutableLiveData<PickOptions?>()
    val pickOptionsLiveData: LiveData<PickOptions?>
        get() = _pickOptionsLiveData
    var pickOptions: PickOptions?
        get() = _pickOptionsLiveData.value
        set(value) {
            _pickOptionsLiveData.value = value
        }

    private val _selectedFilesLiveData = MutableLiveData(fileItemSetOf())
    val selectedFilesLiveData: LiveData<FileItemSet>
        get() = _selectedFilesLiveData
    val selectedFiles: FileItemSet
        get() = _selectedFilesLiveData.valueCompat

    fun selectFile(file: FileItem, selected: Boolean) {
        selectFiles(fileItemSetOf(file), selected)
    }

    fun selectFiles(files: FileItemSet, selected: Boolean) {
        val selectedFiles = _selectedFilesLiveData.valueCompat
        if (selectedFiles === files) {
            if (!selected && selectedFiles.isNotEmpty()) {
                selectedFiles.clear()
                _selectedFilesLiveData.value = selectedFiles
            }
            return
        }
        var changed = false
        for (file in files) {
            changed = changed or if (selected) {
                selectedFiles.add(file)
            } else {
                selectedFiles.remove(file)
            }
        }
        if (changed) {
            _selectedFilesLiveData.value = selectedFiles
        }
    }

    fun replaceSelectedFiles(files: FileItemSet) {
        val selectedFiles = _selectedFilesLiveData.valueCompat
        if (selectedFiles == files) {
            return
        }
        selectedFiles.clear()
        selectedFiles.addAll(files)
        _selectedFilesLiveData.value = selectedFiles
    }

    fun clearSelectedFiles() {
        val selectedFiles = _selectedFilesLiveData.valueCompat
        if (selectedFiles.isEmpty()) {
            return
        }
        selectedFiles.clear()
        _selectedFilesLiveData.value = selectedFiles
    }

    val pasteStateLiveData: LiveData<PasteState> = _pasteStateLiveData
    val pasteState: PasteState
        get() = _pasteStateLiveData.valueCompat

    fun addToPasteState(copy: Boolean, files: FileItemSet) {
        val pasteState = _pasteStateLiveData.valueCompat
        var changed = false
        if (pasteState.copy != copy) {
            changed = pasteState.files.isNotEmpty()
            pasteState.files.clear()
            pasteState.copy = copy
        }
        changed = changed or pasteState.files.addAll(files)
        if (changed) {
            _pasteStateLiveData.value = pasteState
        }
    }

    fun clearPasteState() {
        val pasteState = _pasteStateLiveData.valueCompat
        if (pasteState.files.isEmpty()) {
            return
        }
        pasteState.files.clear()
        _pasteStateLiveData.value = pasteState
    }

    private val _isRequestingStorageAccessLiveData = MutableLiveData(false)
    var isStorageAccessRequested: Boolean
        get() = _isRequestingStorageAccessLiveData.valueCompat
        set(value) {
            _isRequestingStorageAccessLiveData.value = value
        }

    override fun onCleared() {
        _fileListLiveData.close()
    }

    companion object {
        private val _pasteStateLiveData = MutableLiveData(PasteState())
        private const val TAG = "FileListViewModel"
    }
}

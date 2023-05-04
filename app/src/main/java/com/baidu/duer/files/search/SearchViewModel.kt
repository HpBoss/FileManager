package com.baidu.duer.files.search

import android.os.Environment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.baidu.duer.files.file.FileItem
import com.baidu.duer.files.filelist.SearchFileListLiveData
import com.baidu.duer.files.filelist.SearchState
import com.baidu.duer.files.util.CloseableLiveData
import com.baidu.duer.files.util.Failure
import com.baidu.duer.files.util.Stateful
import com.baidu.duer.files.util.valueCompat
import java8.nio.file.Paths
import java.io.Closeable

/**
 * @Author : 何飘
 * @CreateTime : 2023/3/5
 * @Description :
 */
class SearchViewModel : ViewModel() {
    private val _searchStateLiveData = MutableLiveData(SearchState(false, ""))
    val searchState: SearchState
        get() = _searchStateLiveData.valueCompat

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

    private class SearchFileListSwitchMapLiveData(
        private val searchStateLiveData: LiveData<SearchState>,
    ) : MediatorLiveData<Stateful<List<FileItem>>>(), Closeable {
        private var liveData: CloseableLiveData<Stateful<List<FileItem>>>? = null

        init {
            addSource(searchStateLiveData) { updateSource() }
        }

        private fun updateSource() {
            liveData?.let {
                removeSource(it)
                it.close()
            }
            val searchState = searchStateLiveData.valueCompat
            val liveData = if (searchState.isSearching) {
                SearchFileListLiveData(
                    Paths.get(Environment.getExternalStorageDirectory().absolutePath),
                    searchState.query
                )
            } else {
                null
            }
            this.liveData = liveData
            liveData?.let { data ->
                addSource(data as CloseableLiveData<Stateful<List<FileItem>>>) { value = it }
            } ?: run { value = Failure(arrayListOf(), Exception("stop search, data is null")) }

        }

        override fun close() {
            liveData?.let {
                removeSource(it)
                it.close()
                this.liveData = null
            }
        }
    }

    private val _searchFileListLiveData = SearchFileListSwitchMapLiveData(_searchStateLiveData)
    val searchFileListLiveData: LiveData<Stateful<List<FileItem>>>
        get() = _searchFileListLiveData

    val searchResultHint = MutableLiveData<String>()
}
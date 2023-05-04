package com.baidu.duer.files.fileproperties

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.baidu.duer.files.file.FileItem
import com.baidu.duer.files.util.Stateful

class FilePropertiesFileViewModel(file: FileItem) : ViewModel() {
    private val _fileLiveData = FileLiveData(file)
    val fileLiveData: LiveData<Stateful<FileItem>>
        get() = _fileLiveData

    fun reload() {
        _fileLiveData.loadValue()
    }

    override fun onCleared() {
        _fileLiveData.close()
    }
}

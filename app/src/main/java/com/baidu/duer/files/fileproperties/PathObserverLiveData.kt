package com.baidu.duer.files.fileproperties

import com.baidu.duer.files.filelist.PathObserver
import com.baidu.duer.files.util.CloseableLiveData
import java8.nio.file.Path

abstract class PathObserverLiveData<T>(protected val path: Path) : CloseableLiveData<T>() {
    private lateinit var observer: PathObserver

    protected fun observe() {
        observer = PathObserver(path) { onChangeObserved() }
    }

    abstract override fun loadValue()

    private fun onChangeObserved() {
        if (hasActiveObservers()) {
            loadValue()
        } else {
            isChangedWhileInactive = true
        }
    }
}

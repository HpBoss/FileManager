package com.baidu.duer.files.fileproperties.video

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.baidu.duer.files.util.Stateful
import java8.nio.file.Path

class FilePropertiesVideoTabViewModel(path: Path) : ViewModel() {
    private val _videoInfoLiveData = VideoInfoLiveData(path)
    val videoInfoLiveData: LiveData<Stateful<VideoInfo>>
        get() = _videoInfoLiveData

    fun reload() {
        _videoInfoLiveData.loadValue()
    }

    override fun onCleared() {
        _videoInfoLiveData.close()
    }
}

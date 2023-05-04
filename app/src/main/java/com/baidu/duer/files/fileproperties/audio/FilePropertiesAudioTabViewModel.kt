package com.baidu.duer.files.fileproperties.audio

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.baidu.duer.files.util.Stateful
import java8.nio.file.Path

class FilePropertiesAudioTabViewModel(path: Path) : ViewModel() {
    private val _audioInfoLiveData = AudioInfoLiveData(path)
    val audioInfoLiveData: LiveData<Stateful<AudioInfo>>
        get() = _audioInfoLiveData

    fun reload() {
        _audioInfoLiveData.loadValue()
    }

    override fun onCleared() {
        _audioInfoLiveData.close()
    }
}

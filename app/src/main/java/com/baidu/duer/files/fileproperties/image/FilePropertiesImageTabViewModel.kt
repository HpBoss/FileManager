package com.baidu.duer.files.fileproperties.image

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.baidu.duer.files.file.MimeType
import com.baidu.duer.files.util.Stateful
import java8.nio.file.Path

class FilePropertiesImageTabViewModel(path: Path, mimeType: MimeType) : ViewModel() {
    private val _imageInfoLiveData = ImageInfoLiveData(path, mimeType)
    val imageInfoLiveData: LiveData<Stateful<ImageInfo>>
        get() = _imageInfoLiveData

    fun reload() {
        _imageInfoLiveData.loadValue()
    }

    override fun onCleared() {
        _imageInfoLiveData.close()
    }
}

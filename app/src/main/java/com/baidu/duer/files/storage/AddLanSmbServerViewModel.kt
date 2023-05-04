package com.baidu.duer.files.storage

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.baidu.duer.files.util.Stateful

class AddLanSmbServerViewModel : ViewModel() {
    private val _lanSmbServerListLiveData = LanSmbServerListLiveData()
    val lanSmbServerListLiveData: LiveData<Stateful<List<LanSmbServer>>> = _lanSmbServerListLiveData

    fun reload() {
        _lanSmbServerListLiveData.loadValue()
    }

    override fun onCleared() {
        _lanSmbServerListLiveData.close()
    }
}

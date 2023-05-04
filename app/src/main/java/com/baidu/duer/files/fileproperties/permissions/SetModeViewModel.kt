package com.baidu.duer.files.fileproperties.permissions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.baidu.duer.files.provider.common.PosixFileModeBit
import com.baidu.duer.files.util.toEnumSet
import com.baidu.duer.files.util.valueCompat

class SetModeViewModel(mode: Set<PosixFileModeBit>) : ViewModel() {
    private val _modeLiveData: MutableLiveData<Set<PosixFileModeBit>> = MutableLiveData(mode)
    val modeLiveData: LiveData<Set<PosixFileModeBit>>
        get() = _modeLiveData
    val mode: Set<PosixFileModeBit>
        get() = _modeLiveData.valueCompat

    fun toggleModeBit(modeBit: PosixFileModeBit) {
        val mode = _modeLiveData.valueCompat.toEnumSet()
        if (modeBit in mode) {
            mode -= modeBit
        } else {
            mode += modeBit
        }
        _modeLiveData.value = mode
    }
}

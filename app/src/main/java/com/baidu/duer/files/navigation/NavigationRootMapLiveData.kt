package com.baidu.duer.files.navigation

import androidx.lifecycle.MediatorLiveData
import com.baidu.duer.files.util.valueCompat
import java8.nio.file.Path

object NavigationRootMapLiveData : MediatorLiveData<Map<Path, NavigationRoot>>() {
    init {
        // Initialize value before we have any active observer.
        loadValue()
        addSource(NavigationItemListLiveData) { loadValue() }
    }

    private fun loadValue() {
        value = NavigationItemListLiveData.valueCompat
            .mapNotNull { it as? NavigationRoot }
            .associateBy { it.path }
    }
}

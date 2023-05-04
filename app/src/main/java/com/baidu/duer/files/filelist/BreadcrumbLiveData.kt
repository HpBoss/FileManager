package com.baidu.duer.files.filelist

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.baidu.duer.files.navigation.NavigationRootMapLiveData
import com.baidu.duer.files.util.valueCompat
import java8.nio.file.Path

class BreadcrumbLiveData(
    private val trailLiveData: LiveData<TrailData>,
    tabTypeLiveData: LiveData<Int> = MutableLiveData()
) : MediatorLiveData<BreadcrumbData>() {
    init {
        addSource(trailLiveData) { loadValue() }
        addSource(NavigationRootMapLiveData) { loadValue() }
        // 当tabType发生变化时，希望此时BreadcrumbLayout是一个不展示面包屑路径的状态
        addSource(tabTypeLiveData) { loadValueOfTab() }
    }

    private fun loadValueOfTab() {
        value = BreadcrumbData(mutableListOf(), mutableListOf(), 0)
    }

    private fun loadValue() {
        val navigationRootMap = NavigationRootMapLiveData.valueCompat
        val trailData = trailLiveData.valueCompat
        val paths = mutableListOf<Path>()
        val nameProducers = mutableListOf<(Context) -> String>()
        var selectedIndex = trailData.currentIndex
        for (path in trailData.trail) {
            val navigationRoot = navigationRootMap[path]
            val itemCount = nameProducers.size
            if (navigationRoot != null && selectedIndex >= itemCount) {
                selectedIndex -= itemCount
                paths.clear()
                paths.add(navigationRoot.path)
                nameProducers.clear()
                nameProducers.add { navigationRoot.getName(it) }
            } else {
                paths.add(path)
                nameProducers.add { path.name }
            }
        }
        value = BreadcrumbData(paths, nameProducers, selectedIndex)
    }
}

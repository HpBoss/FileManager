package com.baidu.duer.files.settings

import com.baidu.duer.files.R
import com.baidu.duer.files.filelist.FileSortOptions
import java8.nio.file.Path

object PathSettings {
    private const val NAME_SUFFIX = "path"
    private const val TAB_NAME_SUFFIX = "tab"

    @Suppress("UNCHECKED_CAST")
    fun getFileListSortOptions(path: Path): SettingLiveData<FileSortOptions?> =
        ParcelValueSettingLiveData(
            NAME_SUFFIX, R.string.pref_key_file_list_sort_options, path.toString(), null
        )

    fun getFileListSortOptions(tabType: Int): SettingLiveData<FileSortOptions?> =
        ParcelValueSettingLiveData(
            TAB_NAME_SUFFIX,
            R.string.pref_key_file_list_sort_options_by_tab,
            tabType.toString(),
            null
        )
}

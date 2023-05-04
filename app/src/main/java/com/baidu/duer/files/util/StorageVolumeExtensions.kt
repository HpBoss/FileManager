package com.baidu.duer.files.util

import android.os.storage.StorageVolume
import com.baidu.duer.files.compat.directoryCompat

val StorageVolume.isMounted: Boolean
    get() = directoryCompat != null

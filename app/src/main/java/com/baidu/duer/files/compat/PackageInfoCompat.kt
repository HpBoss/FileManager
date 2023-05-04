package com.baidu.duer.files.compat

import android.content.pm.PackageInfo
import androidx.core.content.pm.PackageInfoCompat

val PackageInfo.longVersionCodeCompat: Long
    get() = PackageInfoCompat.getLongVersionCode(this)

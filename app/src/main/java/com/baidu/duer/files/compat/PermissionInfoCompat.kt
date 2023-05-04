package com.baidu.duer.files.compat

import android.content.pm.PermissionInfo
import androidx.core.content.pm.PermissionInfoCompat

val PermissionInfo.protectionCompat: Int
    get() = PermissionInfoCompat.getProtection(this)

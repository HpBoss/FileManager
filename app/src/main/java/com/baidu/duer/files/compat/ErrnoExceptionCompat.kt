package com.baidu.duer.files.compat

import android.system.ErrnoException
import com.baidu.duer.files.hiddenapi.RestrictedHiddenApi
import com.baidu.duer.files.util.lazyReflectedField

@RestrictedHiddenApi
private val functionNameField by lazyReflectedField(ErrnoException::class.java, "functionName")

val ErrnoException.functionNameCompat: String
    get() = functionNameField.get(this) as String

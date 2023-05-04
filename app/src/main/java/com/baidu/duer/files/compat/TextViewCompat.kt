package com.baidu.duer.files.compat

import android.widget.TextView
import androidx.annotation.StyleRes
import androidx.core.widget.TextViewCompat

fun TextView.setTextAppearanceCompat(@StyleRes resId: Int) {
    TextViewCompat.setTextAppearance(this, resId)
}

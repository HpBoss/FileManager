package com.baidu.duer.files.compat

import android.os.Build
import android.text.method.DigitsKeyListener
import java.util.*

object DigitsKeyListenerCompat {
    fun getInstance(locale: Locale?, sign: Boolean, decimal: Boolean): DigitsKeyListener =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DigitsKeyListener.getInstance(locale, sign, decimal)
        } else {
            @Suppress("DEPRECATION")
            DigitsKeyListener.getInstance(sign, decimal)
        }
}

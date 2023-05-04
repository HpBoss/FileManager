package com.baidu.duer.files.settings

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.preference.ListPreference
import java.nio.charset.Charset

class CharsetPreference : ListPreference {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    )

    constructor(
        context: Context,
        attrs: AttributeSet?,
        @AttrRes defStyleAttr: Int,
        @StyleRes defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    init {
        val charsets = Charset.availableCharsets()
        entries = charsets.values.map { it.displayName() }.toTypedArray<CharSequence>()
        entryValues = charsets.keys.toTypedArray<CharSequence>()
    }
}

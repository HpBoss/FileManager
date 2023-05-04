package com.baidu.duer.files.settings

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import com.baidu.duer.files.util.valueCompat
import java8.nio.file.Path

class DefaultDirectoryPreference : PathPreference {
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

    override var persistedPath: Path
        get() = Settings.FILE_LIST_DEFAULT_DIRECTORY.valueCompat
        set(value) {
            Settings.FILE_LIST_DEFAULT_DIRECTORY.putValue(value)
        }
}

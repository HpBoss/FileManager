package com.baidu.duer.files.filelist

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// TODO: Make immutable?
@Parcelize
class PasteState(
    var copy: Boolean = false,
    val files: FileItemSet = fileItemSetOf()
) : Parcelable

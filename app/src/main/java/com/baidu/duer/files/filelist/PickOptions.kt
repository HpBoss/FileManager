package com.baidu.duer.files.filelist

import com.baidu.duer.files.file.MimeType

class PickOptions(
    val readOnly: Boolean,
    val pickDirectory: Boolean,
    val mimeTypes: List<MimeType>,
    val localOnly: Boolean,
    val allowMultiple: Boolean
)

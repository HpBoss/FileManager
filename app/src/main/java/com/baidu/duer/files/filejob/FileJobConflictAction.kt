package com.baidu.duer.files.filejob

enum class FileJobConflictAction {
    MERGE_OR_REPLACE,
    RENAME,
    SKIP,
    CANCEL,
    CANCELED
}

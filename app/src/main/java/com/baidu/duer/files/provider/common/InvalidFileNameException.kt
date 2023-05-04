package com.baidu.duer.files.provider.common

import java8.nio.file.FileSystemException

class InvalidFileNameException : FileSystemException {
    constructor(file: String?) : super(file)

    constructor(file: String?, other: String?, reason: String?) : super(file, other, reason)
}

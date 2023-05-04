package com.baidu.duer.files.provider.remote

import java.io.IOException

class RemoteFileSystemException : IOException {
    constructor()

    constructor(message: String?) : super(message)

    constructor(message: String?, cause: Throwable?) : super(message, cause)

    constructor(cause: Throwable?) : super(cause)
}

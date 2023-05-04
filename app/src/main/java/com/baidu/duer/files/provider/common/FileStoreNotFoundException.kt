package com.baidu.duer.files.provider.common

import java8.nio.file.FileSystemException

class FileStoreNotFoundException(file: String?) : FileSystemException(file)

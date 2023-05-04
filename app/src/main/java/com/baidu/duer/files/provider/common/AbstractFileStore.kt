package com.baidu.duer.files.provider.common

import java8.nio.file.FileStore
import java8.nio.file.attribute.FileStoreAttributeView
import java.io.IOException

abstract class AbstractFileStore : FileStore() {
    override fun <V : FileStoreAttributeView?> getFileStoreAttributeView(type: Class<V>): V? = null

    @Throws(IOException::class)
    override fun getAttribute(attribute: String): Any {
        throw UnsupportedOperationException()
    }
}

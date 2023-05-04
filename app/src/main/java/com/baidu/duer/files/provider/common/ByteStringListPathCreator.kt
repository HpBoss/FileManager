package com.baidu.duer.files.provider.common

import java8.nio.file.FileSystem

interface ByteStringListPathCreator {
    fun getPath(first: ByteString, vararg more: ByteString): ByteStringListPath<*>
}

fun FileSystem.getPath(first: ByteString, vararg more: ByteString): ByteStringListPath<*> =
    (this as ByteStringListPathCreator).getPath(first, *more)

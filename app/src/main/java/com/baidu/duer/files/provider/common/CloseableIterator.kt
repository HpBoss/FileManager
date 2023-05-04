package com.baidu.duer.files.provider.common

import java.io.Closeable

interface CloseableIterator<T> : Iterator<T>, Closeable

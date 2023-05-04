package com.baidu.duer.files.provider.linux

import com.baidu.duer.files.provider.common.AbstractWatchKey

internal class LocalLinuxWatchKey(
    watchService: LocalLinuxWatchService,
    path: LinuxPath,
    val watchDescriptor: Int
) : AbstractWatchKey<LocalLinuxWatchKey, LinuxPath>(watchService, path)

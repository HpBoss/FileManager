package com.baidu.duer.files.provider.common

import java8.nio.file.Path

class PollingWatchKey(
    watchService: PollingWatchService,
    path: Path
) : AbstractWatchKey<PollingWatchKey, Path>(watchService, path)

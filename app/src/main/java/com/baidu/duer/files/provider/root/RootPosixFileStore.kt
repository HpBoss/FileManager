package com.baidu.duer.files.provider.root

import com.baidu.duer.files.provider.common.PosixFileStore
import com.baidu.duer.files.provider.remote.RemoteInterface
import com.baidu.duer.files.provider.remote.RemotePosixFileStore

class RootPosixFileStore(fileStore: PosixFileStore) : RemotePosixFileStore(
    RemoteInterface { RootFileService.getRemotePosixFileStoreInterface(fileStore) }
)

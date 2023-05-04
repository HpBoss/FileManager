package com.baidu.duer.files.provider.root

import com.baidu.duer.files.provider.common.PosixFileAttributeView
import com.baidu.duer.files.provider.remote.RemoteInterface
import com.baidu.duer.files.provider.remote.RemotePosixFileAttributeView

open class RootPosixFileAttributeView(
    attributeView: PosixFileAttributeView
) : RemotePosixFileAttributeView(
    RemoteInterface { RootFileService.getRemotePosixFileAttributeViewInterface(attributeView) }
) {
    override fun name(): String {
        throw AssertionError()
    }
}

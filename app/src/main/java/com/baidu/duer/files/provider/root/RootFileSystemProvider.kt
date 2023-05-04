package com.baidu.duer.files.provider.root

import com.baidu.duer.files.provider.remote.RemoteFileSystemProvider
import com.baidu.duer.files.provider.remote.RemoteInterface
import java8.nio.file.FileSystem
import java8.nio.file.LinkOption
import java8.nio.file.Path
import java8.nio.file.attribute.FileAttributeView
import java.net.URI

open class RootFileSystemProvider(scheme: String) : RemoteFileSystemProvider(
    RemoteInterface { RootFileService.getRemoteFileSystemProviderInterface(scheme) }
) {
    override fun getScheme(): String {
        throw AssertionError()
    }

    override fun newFileSystem(uri: URI, env: Map<String, *>): FileSystem {
        throw AssertionError()
    }

    override fun getFileSystem(uri: URI): FileSystem {
        throw AssertionError()
    }

    override fun getPath(uri: URI): Path {
        throw AssertionError()
    }

    override fun <V : FileAttributeView> getFileAttributeView(
        path: Path,
        type: Class<V>,
        vararg options: LinkOption
    ): V? {
        throw AssertionError()
    }
}

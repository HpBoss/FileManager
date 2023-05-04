package com.baidu.duer.files.provider.remote

import com.baidu.duer.files.provider.FileSystemProviders
import com.baidu.duer.files.provider.archive.archiveRefresh
import java8.nio.file.FileSystem

open class RemoteFileServiceInterface : IRemoteFileService.Stub() {
    override fun getRemoteFileSystemProviderInterface(scheme: String): IRemoteFileSystemProvider =
        RemoteFileSystemProviderInterface(FileSystemProviders[scheme])

    override fun getRemoteFileSystemInterface(fileSystem: ParcelableObject): IRemoteFileSystem =
        RemoteFileSystemInterface(fileSystem.value())

    override fun getRemotePosixFileStoreInterface(
        fileStore: ParcelableObject
    ): IRemotePosixFileStore = RemotePosixFileStoreInterface(fileStore.value())

    override fun getRemotePosixFileAttributeViewInterface(
        attributeView: ParcelableObject
    ): IRemotePosixFileAttributeView =
        RemotePosixFileAttributeViewInterface(attributeView.value())

    override fun refreshArchiveFileSystem(fileSystem: ParcelableObject) {
        fileSystem.value<FileSystem>().getPath("").archiveRefresh()
    }
}

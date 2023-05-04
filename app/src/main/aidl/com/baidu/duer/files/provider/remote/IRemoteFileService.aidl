package com.baidu.duer.files.provider.remote;

import com.baidu.duer.files.provider.remote.IRemoteFileSystem;
import com.baidu.duer.files.provider.remote.IRemoteFileSystemProvider;
import com.baidu.duer.files.provider.remote.IRemotePosixFileAttributeView;
import com.baidu.duer.files.provider.remote.IRemotePosixFileStore;
import com.baidu.duer.files.provider.remote.ParcelableObject;

interface IRemoteFileService {
    IRemoteFileSystemProvider getRemoteFileSystemProviderInterface(String scheme);

    IRemoteFileSystem getRemoteFileSystemInterface(in ParcelableObject fileSystem);

    IRemotePosixFileStore getRemotePosixFileStoreInterface(in ParcelableObject fileStore);

    IRemotePosixFileAttributeView getRemotePosixFileAttributeViewInterface(
        in ParcelableObject attributeView
    );

    void refreshArchiveFileSystem(in ParcelableObject fileSystem);
}

package com.baidu.duer.files.provider.remote;

import com.baidu.duer.files.provider.remote.ParcelableException;

interface IRemoteFileSystem {
    void close(out ParcelableException exception);
}

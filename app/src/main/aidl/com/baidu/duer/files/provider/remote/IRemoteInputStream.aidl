package com.baidu.duer.files.provider.remote;

import com.baidu.duer.files.provider.remote.ParcelableException;

interface IRemoteInputStream {
    int read(out ParcelableException exception);

    int read2(out byte[] buffer, out ParcelableException exception);

    long skip(long size, out ParcelableException exception);

    int available(out ParcelableException exception);

    void close(out ParcelableException exception);
}

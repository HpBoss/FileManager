package com.baidu.duer.files.provider.remote;

import com.baidu.duer.files.provider.remote.ParcelableException;
import com.baidu.duer.files.util.RemoteCallback;

interface IRemotePathObservable {
    void addObserver(in RemoteCallback observer);

    void close(out ParcelableException exception);
}

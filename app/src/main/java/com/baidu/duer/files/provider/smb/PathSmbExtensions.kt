package com.baidu.duer.files.provider.smb

import com.baidu.duer.files.provider.smb.client.Authority
import java8.nio.file.Path

fun Authority.createSmbRootPath(): Path =
    SmbFileSystemProvider.getOrNewFileSystem(this).rootDirectory

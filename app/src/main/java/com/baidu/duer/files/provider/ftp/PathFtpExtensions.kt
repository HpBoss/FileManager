package com.baidu.duer.files.provider.ftp

import com.baidu.duer.files.provider.ftp.client.Authority
import java8.nio.file.Path

fun Authority.createFtpRootPath(): Path =
    FtpFileSystemProvider.getOrNewFileSystem(this).rootDirectory

package com.baidu.duer.files.provider.sftp

import com.baidu.duer.files.provider.sftp.client.Authority
import java8.nio.file.Path

fun Authority.createSftpRootPath(): Path =
    SftpFileSystemProvider.getOrNewFileSystem(this).rootDirectory

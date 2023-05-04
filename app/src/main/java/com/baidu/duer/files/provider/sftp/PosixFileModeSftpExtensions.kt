package com.baidu.duer.files.provider.sftp

import com.baidu.duer.files.provider.common.PosixFileModeBit
import com.baidu.duer.files.provider.common.toInt
import net.schmizz.sshj.sftp.FileAttributes

fun Set<PosixFileModeBit>.toSftpAttributes(): FileAttributes =
    FileAttributes.Builder().withPermissions(toInt()).build()

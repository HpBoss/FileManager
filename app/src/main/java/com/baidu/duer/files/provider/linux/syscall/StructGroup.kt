package com.baidu.duer.files.provider.linux.syscall

import com.baidu.duer.files.provider.common.ByteString

class StructGroup(
    val gr_name: ByteString?,
    val gr_passwd: ByteString?,
    val gr_gid: Int,
    val gr_mem: Array<ByteString>?
)

package com.baidu.duer.files.provider.linux.syscall

import com.baidu.duer.files.provider.common.ByteString

class StructInotifyEvent(
    val wd: Int,
    val mask: Int, /* uint32_t */
    val cookie: Int, /* uint32_t */
    val name: ByteString?
)

package com.baidu.duer.files.provider.linux.syscall

import android.os.Parcelable
import com.baidu.duer.files.provider.common.ByteString
import kotlinx.parcelize.Parcelize

@Parcelize
class StructMntent(
    val mnt_fsname: ByteString,
    val mnt_dir: ByteString,
    val mnt_type: ByteString,
    val mnt_opts: ByteString,
    val mnt_freq: Int,
    val mnt_passno: Int
) : Parcelable

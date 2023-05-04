package com.baidu.duer.files.provider.linux.syscall

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * @see android.system.StructTimespec
 */
@Parcelize
class StructTimespec(
    val tv_sec: Long, /*time_t*/
    val tv_nsec: Long
) : Parcelable

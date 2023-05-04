package com.baidu.duer.files.provider.common

import android.os.ParcelFileDescriptor
import com.baidu.duer.files.compat.NioUtilsCompat
import com.baidu.duer.files.provider.linux.syscall.SyscallException
import com.baidu.duer.files.provider.linux.syscall.Syscalls
import java8.nio.channels.FileChannel
import java8.nio.channels.FileChannels
import java.io.Closeable
import java.io.FileDescriptor
import java.io.IOException
import kotlin.reflect.KClass

fun KClass<FileChannel>.open(fd: FileDescriptor, flags: Int): FileChannel {
    val closeable = Closeable {
        try {
            Syscalls.close(fd)
        } catch (e: SyscallException) {
            throw IOException(e)
        }
    }
    return FileChannels.from(NioUtilsCompat.newFileChannel(closeable, fd, flags))
}

fun KClass<FileChannel>.open(pfd: ParcelFileDescriptor, mode: String): FileChannel =
    FileChannels.from(
        NioUtilsCompat.newFileChannel(
            pfd, pfd.fileDescriptor,
            ParcelFileDescriptor::class.modeToFlags(ParcelFileDescriptor.parseMode(mode))
        )
    )

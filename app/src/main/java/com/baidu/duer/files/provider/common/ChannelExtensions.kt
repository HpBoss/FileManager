package com.baidu.duer.files.provider.common

import java.io.InputStream
import java.io.OutputStream
import java.nio.channels.Channels
import java.nio.channels.ReadableByteChannel
import java.nio.channels.WritableByteChannel

fun ReadableByteChannel.newInputStream(): InputStream = Channels.newInputStream(this)

fun WritableByteChannel.newOutputStream(): OutputStream = Channels.newOutputStream(this)

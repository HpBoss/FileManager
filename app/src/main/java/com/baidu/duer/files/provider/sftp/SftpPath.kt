package com.baidu.duer.files.provider.sftp

import android.os.Parcel
import android.os.Parcelable
import com.baidu.duer.files.provider.common.ByteString
import com.baidu.duer.files.provider.common.ByteStringListPath
import com.baidu.duer.files.provider.common.LocalWatchService
import com.baidu.duer.files.provider.common.UriAuthority
import com.baidu.duer.files.provider.sftp.client.Authority
import com.baidu.duer.files.provider.sftp.client.Client
import com.baidu.duer.files.util.readParcelable
import java8.nio.file.*
import java.io.File
import java.io.IOException

internal class SftpPath : ByteStringListPath<SftpPath>, Client.Path {
    private val fileSystem: SftpFileSystem

    constructor(
        fileSystem: SftpFileSystem,
        path: ByteString
    ) : super(SftpFileSystem.SEPARATOR, path) {
        this.fileSystem = fileSystem
    }

    private constructor(
        fileSystem: SftpFileSystem,
        absolute: Boolean,
        segments: List<ByteString>
    ) : super(SftpFileSystem.SEPARATOR, absolute, segments) {
        this.fileSystem = fileSystem
    }

    override fun isPathAbsolute(path: ByteString): Boolean =
        path.isNotEmpty() && path[0] == SftpFileSystem.SEPARATOR

    override fun createPath(path: ByteString): SftpPath = SftpPath(fileSystem, path)

    override fun createPath(absolute: Boolean, segments: List<ByteString>): SftpPath =
        SftpPath(fileSystem, absolute, segments)

    override val uriAuthority: UriAuthority
        get() = fileSystem.authority.toUriAuthority()

    override val defaultDirectory: SftpPath
        get() = fileSystem.defaultDirectory

    override fun getFileSystem(): FileSystem = fileSystem

    override fun getRoot(): SftpPath? = if (isAbsolute) fileSystem.rootDirectory else null

    @Throws(IOException::class)
    override fun toRealPath(vararg options: LinkOption): SftpPath {
        throw UnsupportedOperationException()
    }

    override fun toFile(): File {
        throw UnsupportedOperationException()
    }

    @Throws(IOException::class)
    override fun register(
        watcher: WatchService,
        events: Array<WatchEvent.Kind<*>>,
        vararg modifiers: WatchEvent.Modifier
    ): WatchKey {
        if (watcher !is LocalWatchService) {
            throw ProviderMismatchException(watcher.toString())
        }
        return watcher.register(this, events, *modifiers)
    }

    override val authority: Authority
        get() = fileSystem.authority

    override val remotePath: String
        get() = toString()

    private constructor(source: Parcel) : super(source) {
        fileSystem = source.readParcelable()!!
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)

        dest.writeParcelable(fileSystem, flags)
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<SftpPath> {
            override fun createFromParcel(source: Parcel): SftpPath = SftpPath(source)

            override fun newArray(size: Int): Array<SftpPath?> = arrayOfNulls(size)
        }
    }
}

val Path.isSftpPath: Boolean
    get() = this is SftpPath

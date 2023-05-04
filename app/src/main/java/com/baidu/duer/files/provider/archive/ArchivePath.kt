package com.baidu.duer.files.provider.archive

import android.os.Parcel
import android.os.Parcelable
import com.baidu.duer.files.provider.common.ByteString
import com.baidu.duer.files.provider.common.ByteStringListPath
import com.baidu.duer.files.provider.common.toByteString
import com.baidu.duer.files.provider.root.RootablePath
import com.baidu.duer.files.util.readParcelable
import java8.nio.file.*
import java.io.File
import java.io.IOException

internal class ArchivePath : ByteStringListPath<ArchivePath>, RootablePath {
    private val fileSystem: ArchiveFileSystem

    constructor(fileSystem: ArchiveFileSystem, path: ByteString) : super(
        ArchiveFileSystem.SEPARATOR, path
    ) {
        this.fileSystem = fileSystem
    }

    private constructor(
        fileSystem: ArchiveFileSystem,
        absolute: Boolean,
        segments: List<ByteString>
    ) : super(ArchiveFileSystem.SEPARATOR, absolute, segments) {
        this.fileSystem = fileSystem
    }

    override fun isPathAbsolute(path: ByteString): Boolean =
        !path.isEmpty() && path[0] == ArchiveFileSystem.SEPARATOR

    override fun createPath(path: ByteString): ArchivePath = ArchivePath(fileSystem, path)

    override fun createPath(absolute: Boolean, segments: List<ByteString>): ArchivePath =
        ArchivePath(fileSystem, absolute, segments)

    override val uriPath: ByteString
        // Prepend a slash character to make it a valid URI path, since we always have an (empty)
        // authority.
        get() = ("/" + fileSystem.archiveFile.toUri().toString()).toByteString()

    override val uriQuery: ByteString?
        get() = super.uriPath

    override val defaultDirectory: ArchivePath
        get() = fileSystem.defaultDirectory

    override fun getFileSystem(): ArchiveFileSystem = fileSystem

    override fun getRoot(): ArchivePath? = if (isAbsolute) fileSystem.rootDirectory else null

    @Throws(IOException::class)
    override fun toRealPath(vararg options: LinkOption): ArchivePath {
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
        throw UnsupportedOperationException()
    }

    override fun isRootRequired(isAttributeAccess: Boolean): Boolean {
        val archiveFile = fileSystem.archiveFile
        return if (archiveFile is RootablePath) {
            archiveFile.isRootRequired(isAttributeAccess)
        } else {
            false
        }
    }

    private constructor(source: Parcel) : super(source) {
        fileSystem = source.readParcelable()!!
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)

        dest.writeParcelable(fileSystem, flags)
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<ArchivePath> {
            override fun createFromParcel(source: Parcel): ArchivePath = ArchivePath(source)

            override fun newArray(size: Int): Array<ArchivePath?> = arrayOfNulls(size)
        }
    }
}

val Path.isArchivePath: Boolean
    get() = this is ArchivePath

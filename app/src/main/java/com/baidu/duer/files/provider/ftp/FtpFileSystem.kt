package com.baidu.duer.files.provider.ftp

import android.os.Parcel
import android.os.Parcelable
import com.baidu.duer.files.provider.common.*
import com.baidu.duer.files.provider.ftp.client.Authority
import com.baidu.duer.files.util.readParcelable
import java8.nio.file.*
import java8.nio.file.attribute.UserPrincipalLookupService
import java8.nio.file.spi.FileSystemProvider
import java.io.IOException

internal class FtpFileSystem(
    private val provider: FtpFileSystemProvider,
    val authority: Authority
) : FileSystem(), ByteStringListPathCreator, Parcelable {
    val rootDirectory = FtpPath(this, SEPARATOR_BYTE_STRING)

    init {
        if (!rootDirectory.isAbsolute) {
            throw AssertionError("Root directory must be absolute")
        }
        if (rootDirectory.nameCount != 0) {
            throw AssertionError("Root directory must contain no names")
        }
    }

    private val lock = Any()

    private var isOpen = true

    val defaultDirectory: FtpPath
        get() = rootDirectory

    override fun provider(): FileSystemProvider = provider

    override fun close() {
        synchronized(lock) {
            if (!isOpen) {
                return
            }
            provider.removeFileSystem(this)
            isOpen = false
        }
    }

    override fun isOpen(): Boolean = synchronized(lock) { isOpen }

    override fun isReadOnly(): Boolean = false

    override fun getSeparator(): String = SEPARATOR_STRING

    override fun getRootDirectories(): Iterable<Path> = listOf(rootDirectory)

    override fun getFileStores(): Iterable<FileStore> {
        // TODO
        throw UnsupportedOperationException()
    }

    override fun supportedFileAttributeViews(): Set<String> =
        FtpFileAttributeView.SUPPORTED_NAMES

    override fun getPath(first: String, vararg more: String): FtpPath {
        val path = ByteStringBuilder(first.toByteString())
            .apply { more.forEach { append(SEPARATOR).append(it.toByteString()) } }
            .toByteString()
        return FtpPath(this, path)
    }

    override fun getPath(first: ByteString, vararg more: ByteString): FtpPath {
        val path = ByteStringBuilder(first)
            .apply { more.forEach { append(SEPARATOR).append(it) } }
            .toByteString()
        return FtpPath(this, path)
    }

    override fun getPathMatcher(syntaxAndPattern: String): PathMatcher {
        throw UnsupportedOperationException()
    }

    override fun getUserPrincipalLookupService(): UserPrincipalLookupService {
        throw UnsupportedOperationException()
    }

    @Throws(IOException::class)
    override fun newWatchService(): WatchService = LocalWatchService()

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (javaClass != other?.javaClass) {
            return false
        }
        other as FtpFileSystem
        return authority == other.authority
    }

    override fun hashCode(): Int = authority.hashCode()

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeParcelable(authority, flags)
    }

    companion object {
        const val SEPARATOR = '/'.code.toByte()
        private val SEPARATOR_BYTE_STRING = SEPARATOR.toByteString()
        private const val SEPARATOR_STRING = SEPARATOR.toInt().toChar().toString()

        @JvmField
        val CREATOR = object : Parcelable.Creator<FtpFileSystem> {
            override fun createFromParcel(source: Parcel): FtpFileSystem {
                val authority = source.readParcelable<Authority>()!!
                return FtpFileSystemProvider.getOrNewFileSystem(authority)
            }

            override fun newArray(size: Int): Array<FtpFileSystem?> = arrayOfNulls(size)
        }
    }
}
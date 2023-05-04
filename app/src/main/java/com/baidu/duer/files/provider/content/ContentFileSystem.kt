package com.baidu.duer.files.provider.content

import android.os.Parcel
import android.os.Parcelable
import androidx.core.net.toUri
import com.baidu.duer.files.provider.common.ByteString
import com.baidu.duer.files.provider.common.ByteStringListPathCreator
import java8.nio.file.*
import java8.nio.file.attribute.UserPrincipalLookupService
import java8.nio.file.spi.FileSystemProvider
import java.io.IOException

internal class ContentFileSystem(private val provider: ContentFileSystemProvider) : FileSystem(),
    ByteStringListPathCreator, Parcelable {
    override fun provider(): FileSystemProvider = provider

    override fun close() {
        throw UnsupportedOperationException()
    }

    override fun isOpen(): Boolean = true

    override fun isReadOnly(): Boolean = false

    override fun getSeparator(): String {
        throw UnsupportedOperationException()
    }

    override fun getRootDirectories(): Iterable<Path> = emptyList()

    override fun getFileStores(): Iterable<FileStore> {
        // TODO
        throw UnsupportedOperationException()
    }

    override fun supportedFileAttributeViews(): Set<String> =
        ContentFileAttributeView.SUPPORTED_NAMES

    override fun getPath(first: String, vararg more: String): ContentPath {
        if (more.isNotEmpty()) {
            throw UnsupportedOperationException()
        }
        val uri = first.toUri()
        return ContentPath(this, uri)
    }

    override fun getPath(first: ByteString, vararg more: ByteString): ContentPath {
        if (more.isNotEmpty()) {
            throw UnsupportedOperationException()
        }
        val uri = first.toString().toUri()
        return ContentPath(this, uri)
    }

    override fun getPathMatcher(syntaxAndPattern: String): PathMatcher {
        throw UnsupportedOperationException()
    }

    override fun getUserPrincipalLookupService(): UserPrincipalLookupService {
        throw UnsupportedOperationException()
    }

    @Throws(IOException::class)
    override fun newWatchService(): WatchService {
        // TODO
        throw UnsupportedOperationException()
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {}

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<ContentFileSystem> {
            override fun createFromParcel(source: Parcel): ContentFileSystem =
                ContentFileSystemProvider.fileSystem

            override fun newArray(size: Int): Array<ContentFileSystem?> = arrayOfNulls(size)
        }
    }
}

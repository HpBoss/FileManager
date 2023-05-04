package com.baidu.duer.files.provider.archive

import com.baidu.duer.files.provider.archive.archiver.ArchiveReader
import com.baidu.duer.files.provider.common.ByteString
import com.baidu.duer.files.provider.common.ByteStringBuilder
import com.baidu.duer.files.provider.common.ByteStringListPathCreator
import com.baidu.duer.files.provider.common.toByteString
import java8.nio.file.*
import java8.nio.file.attribute.UserPrincipalLookupService
import java8.nio.file.spi.FileSystemProvider
import org.apache.commons.compress.archivers.ArchiveEntry
import java.io.IOException
import java.io.InputStream

internal class LocalArchiveFileSystem(
    private val fileSystem: ArchiveFileSystem,
    private val provider: ArchiveFileSystemProvider,
    val archiveFile: Path
) : FileSystem(), ByteStringListPathCreator {
    val rootDirectory = ArchivePath(fileSystem, SEPARATOR_BYTE_STRING)

    init {
        if (!rootDirectory.isAbsolute) {
            throw AssertionError("Root directory $rootDirectory must be absolute")
        }
        if (rootDirectory.nameCount != 0) {
            throw AssertionError("Root directory $rootDirectory must contain no names")
        }
    }

    val defaultDirectory: ArchivePath
        get() = rootDirectory

    private val lock = Any()

    private var isOpen = true

    private var isRefreshNeeded = true

    private var entries: Map<Path, ArchiveEntry>? = null

    private var tree: Map<Path, List<Path>>? = null

    @Throws(IOException::class)
    fun getEntry(path: Path): ArchiveEntry =
        synchronized(lock) {
            ensureEntriesLocked()
            getEntryLocked(path)
        }

    @Throws(IOException::class)
    private fun getEntryLocked(path: Path): ArchiveEntry =
        synchronized(lock) {
            entries!![path] ?: throw NoSuchFileException(path.toString())
        }

    @Throws(IOException::class)
    fun newInputStream(file: Path): InputStream =
        synchronized(lock) {
            ensureEntriesLocked()
            val entry = getEntryLocked(file)
            ArchiveReader.newInputStream(archiveFile, entry)
        }

    @Throws(IOException::class)
    fun getDirectoryChildren(directory: Path): List<Path> =
        synchronized(lock) {
            ensureEntriesLocked()
            val entry = getEntryLocked(directory)
            if (!entry.isDirectory) {
                throw NotDirectoryException(directory.toString())
            }
            tree!![directory]!!
        }

    @Throws(IOException::class)
    fun readSymbolicLink(link: Path): String =
        synchronized(lock) {
            ensureEntriesLocked()
            val entry = getEntryLocked(link)
            ArchiveReader.readSymbolicLink(archiveFile, entry)
        }

    fun refresh() {
        synchronized(lock) {
            if (!isOpen) {
                throw ClosedFileSystemException()
            }
            isRefreshNeeded = true
        }
    }

    @Throws(IOException::class)
    private fun ensureEntriesLocked() {
        if (!isOpen) {
            throw ClosedFileSystemException()
        }
        if (isRefreshNeeded) {
            val entriesAndTree = ArchiveReader.readEntries(archiveFile, rootDirectory)
            entries = entriesAndTree.first
            tree = entriesAndTree.second
            isRefreshNeeded = false
        }
    }

    override fun provider(): FileSystemProvider = provider

    override fun close() {
        synchronized(lock) {
            if (!isOpen) {
                return
            }
            provider.removeFileSystem(fileSystem)
            isRefreshNeeded = false
            entries = null
            tree = null
            isOpen = false
        }
    }

    override fun isOpen(): Boolean = synchronized(lock) { isOpen }

    override fun isReadOnly(): Boolean = true

    override fun getSeparator(): String = SEPARATOR_STRING

    override fun getRootDirectories(): Iterable<Path> = listOf(rootDirectory)

    override fun getFileStores(): Iterable<FileStore> {
        // TODO
        throw UnsupportedOperationException()
    }

    override fun supportedFileAttributeViews(): Set<String> =
        ArchiveFileAttributeView.SUPPORTED_NAMES

    override fun getPath(first: String, vararg more: String): ArchivePath {
        val path = ByteStringBuilder(first.toByteString())
            .apply { more.forEach { append(SEPARATOR).append(it.toByteString()) } }
            .toByteString()
        return ArchivePath(fileSystem, path)
    }

    override fun getPath(first: ByteString, vararg more: ByteString): ArchivePath {
        val path = ByteStringBuilder(first)
            .apply { more.forEach { append(SEPARATOR).append(it) } }
            .toByteString()
        return ArchivePath(fileSystem, path)
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

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (javaClass != other?.javaClass) {
            return false
        }
        other as LocalArchiveFileSystem
        return archiveFile == other.archiveFile
    }

    override fun hashCode(): Int = archiveFile.hashCode()

    companion object {
        const val SEPARATOR = '/'.code.toByte()
        private val SEPARATOR_BYTE_STRING = SEPARATOR.toByteString()
        private const val SEPARATOR_STRING = SEPARATOR.toInt().toChar().toString()
    }
}

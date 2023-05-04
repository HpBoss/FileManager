package com.baidu.duer.files.provider.smb

import com.baidu.duer.files.provider.smb.client.Client
import com.baidu.duer.files.provider.smb.client.ClientException
import com.baidu.duer.files.provider.smb.client.FileInformation
import com.baidu.duer.files.provider.smb.client.ShareInformation
import com.hierynomus.msfscc.fileinformation.FileBasicInformation
import java8.nio.file.attribute.BasicFileAttributeView
import java8.nio.file.attribute.BasicFileAttributes
import java8.nio.file.attribute.FileTime
import java.io.IOException
import com.hierynomus.msdtyp.FileTime as SmbFileTime

internal class SmbFileAttributeView(
    private val path: SmbPath,
    private val noFollowLinks: Boolean
) : BasicFileAttributeView {
    override fun name(): String = NAME

    @Throws(IOException::class)
    override fun readAttributes(): BasicFileAttributes {
        val pathInformation = try {
            Client.getPathInformation(path, noFollowLinks)
        } catch (e: ClientException) {
            throw e.toFileSystemException(path.toString())
        }
        return when (pathInformation) {
            is FileInformation -> SmbFileAttributes.from(pathInformation, path)
            is ShareInformation -> SmbShareFileAttributes.from(pathInformation, path)
        }
    }

    override fun setTimes(
        lastModifiedTime: FileTime?,
        lastAccessTime: FileTime?,
        createTime: FileTime?
    ) {
        if (createTime == null && lastAccessTime == null && lastModifiedTime == null) {
            return
        }
        val fileInformation = FileBasicInformation(
            createTime.toSmbFileTime(), lastAccessTime.toSmbFileTime(),
            lastModifiedTime.toSmbFileTime(), SmbFileTime(0), 0
        )
        try {
            Client.setFileInformation(path, noFollowLinks, fileInformation)
        } catch (e: ClientException) {
            throw e.toFileSystemException(path.toString())
        }
    }

    private fun FileTime?.toSmbFileTime(): SmbFileTime =
        if (this != null) SmbFileTime.ofEpochMillis(toMillis()) else SmbFileTime(0)

    companion object {
        private val NAME = SmbFileSystemProvider.scheme

        val SUPPORTED_NAMES = setOf("basic", NAME)
    }
}

package com.baidu.duer.files.provider.linux

import com.baidu.duer.files.provider.common.ByteString
import com.baidu.duer.files.provider.common.PosixGroup
import com.baidu.duer.files.provider.common.PosixUser
import com.baidu.duer.files.provider.common.toByteString
import com.baidu.duer.files.provider.linux.syscall.SyscallException
import com.baidu.duer.files.provider.linux.syscall.Syscalls
import java8.nio.file.attribute.UserPrincipalLookupService
import java8.nio.file.attribute.UserPrincipalNotFoundException
import java.io.IOException

internal object LinuxUserPrincipalLookupService : UserPrincipalLookupService() {
    @Throws(IOException::class)
    override fun lookupPrincipalByName(name: String): PosixUser =
        lookupPrincipalByName(name.toByteString())

    @Throws(IOException::class)
    fun lookupPrincipalByName(name: ByteString): PosixUser {
        val passwd = try {
            Syscalls.getpwnam(name)
        } catch (e: SyscallException) {
            throw e.toFileSystemException(null)
        } ?: throw UserPrincipalNotFoundException(name.toString())
        return PosixUser(passwd.pw_uid, passwd.pw_name)
    }

    @Throws(IOException::class)
    fun lookupPrincipalById(id: Int): PosixUser =
        try {
            getUserById(id)
        } catch (e: SyscallException) {
            throw e.toFileSystemException(null)
        }

    @Throws(SyscallException::class)
    fun getUserById(uid: Int): PosixUser {
        val passwd = Syscalls.getpwuid(uid)
        return PosixUser(uid, passwd?.pw_name)
    }

    @Throws(IOException::class)
    override fun lookupPrincipalByGroupName(group: String): PosixGroup =
        lookupPrincipalByGroupName(group.toByteString())

    @Throws(IOException::class)
    fun lookupPrincipalByGroupName(group: ByteString): PosixGroup {
        val groupStruct = try {
            Syscalls.getgrnam(group)
        } catch (e: SyscallException) {
            throw e.toFileSystemException(null)
        } ?: throw UserPrincipalNotFoundException(group.toString())
        return PosixGroup(groupStruct.gr_gid, groupStruct.gr_name)
    }

    @Throws(IOException::class)
    fun lookupPrincipalByGroupId(groupId: Int): PosixGroup =
        try {
            getGroupById(groupId)
        } catch (e: SyscallException) {
            throw e.toFileSystemException(null)
        }

    @Throws(SyscallException::class)
    fun getGroupById(gid: Int): PosixGroup {
        val group = Syscalls.getgrgid(gid)
        return PosixGroup(gid, group?.gr_name)
    }
}

package com.baidu.duer.files.storage

import com.baidu.duer.files.provider.sftp.client.Authentication
import com.baidu.duer.files.provider.sftp.client.Authenticator
import com.baidu.duer.files.provider.sftp.client.Authority
import com.baidu.duer.files.settings.Settings
import com.baidu.duer.files.util.valueCompat

object SftpServerAuthenticator : Authenticator {
    private val transientServers = mutableSetOf<SftpServer>()

    override fun getAuthentication(authority: Authority): Authentication? {
        val server = synchronized(transientServers) {
            transientServers.find { it.authority == authority }
        } ?: Settings.STORAGES.valueCompat.find {
            it is SftpServer && it.authority == authority
        } as SftpServer?
        return server?.authentication
    }

    fun addTransientServer(server: SftpServer) {
        synchronized(transientServers) { transientServers += server }
    }

    fun removeTransientServer(server: SftpServer) {
        synchronized(transientServers) { transientServers -= server }
    }
}

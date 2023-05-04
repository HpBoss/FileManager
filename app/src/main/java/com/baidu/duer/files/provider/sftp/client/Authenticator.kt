package com.baidu.duer.files.provider.sftp.client

interface Authenticator {
    fun getAuthentication(authority: Authority): Authentication?
}

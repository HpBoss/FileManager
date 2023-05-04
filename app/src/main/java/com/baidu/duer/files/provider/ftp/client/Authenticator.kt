package com.baidu.duer.files.provider.ftp.client

interface Authenticator {
    fun getPassword(authority: Authority): String?
}

package com.baidu.duer.files.provider.smb.client

interface Authenticator {
    fun getPassword(authority: Authority): String?
}

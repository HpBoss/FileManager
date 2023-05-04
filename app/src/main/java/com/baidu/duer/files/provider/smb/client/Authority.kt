package com.baidu.duer.files.provider.smb.client

import android.os.Parcelable
import com.baidu.duer.files.provider.common.UriAuthority
import com.baidu.duer.files.util.takeIfNotEmpty
import com.hierynomus.smbj.SMBClient
import kotlinx.parcelize.Parcelize

@Parcelize
data class Authority(
    val host: String,
    val port: Int,
    val username: String,
    val domain: String?
) : Parcelable {
    fun toUriAuthority(): UriAuthority {
        val userInfo = if (domain != null) "$domain\\$username" else username.takeIfNotEmpty()
        val uriPort = port.takeIf { it != DEFAULT_PORT }
        return UriAuthority(userInfo, host, uriPort)
    }

    override fun toString(): String = toUriAuthority().toString()

    companion object {
        const val DEFAULT_PORT = SMBClient.DEFAULT_PORT
    }
}

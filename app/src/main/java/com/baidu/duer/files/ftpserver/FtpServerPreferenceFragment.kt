package com.baidu.duer.files.ftpserver

import android.os.Bundle
import com.baidu.duer.files.R
import com.baidu.duer.files.ui.PreferenceFragmentCompat

class FtpServerPreferenceFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferencesFix(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.ftp_server)
    }
}

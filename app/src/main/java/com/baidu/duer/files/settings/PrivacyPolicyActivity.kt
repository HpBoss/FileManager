package com.baidu.duer.files.settings

import com.baidu.duer.files.R

/**
 * @Author : 何飘
 * @CreateTime : 2023/3/10
 * @Description :
 */
class PrivacyPolicyActivity : BaseWebViewActivity() {
    override val titleRes: Int
        get() = R.string.about_duer_privacy_policy

    override fun loadWebView() {
        super.loadWebView()
        binding.privacyWebView.apply {
            loadUrl("")
        }
    }
}
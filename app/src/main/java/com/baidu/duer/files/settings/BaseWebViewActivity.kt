package com.baidu.duer.files.settings

import android.os.Bundle
import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.StringRes
import com.baidu.duer.files.app.AppActivity
import com.baidu.duer.files.databinding.ActivityBaseWebViewBinding
import com.baidu.duer.files.util.showToast

/**
 * @Author : 何飘
 * @CreateTime : 2023/3/10
 * @Description :
 */
abstract class BaseWebViewActivity : AppActivity() {
    protected val binding by lazy {
        ActivityBaseWebViewBinding.inflate(this.layoutInflater)
    }

    @get:StringRes
    protected abstract val titleRes: Int

    protected open fun loadWebView() {
        binding.privacyWebView.webViewClient = object : WebViewClient() {
            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                view?.visibility = View.GONE
                showToast("当前无网络，无法打开页面")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.itemTopBack.apply {
            itemBackButton.setOnClickListener { finish() }
            itemTitleText.text = getString(titleRes)
        }

        loadWebView()
    }
}
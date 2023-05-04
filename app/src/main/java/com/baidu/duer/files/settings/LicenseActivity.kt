package com.baidu.duer.files.settings

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.core.view.ViewCompat
import com.baidu.duer.files.R
import com.baidu.duer.files.compat.scrollIndicatorsCompat
import com.baidu.duer.files.ui.createHtml
import com.baidu.duer.files.util.*
import de.psdev.licensesdialog.LicensesDialog
import de.psdev.licensesdialog.NoticesXmlParser
import de.psdev.licensesdialog.model.Notices
import kotlinx.parcelize.Parcelize
import java.nio.charset.StandardCharsets

/**
 * @Author : 何飘
 * @CreateTime : 2023/3/10
 * @Description :
 */
class LicenseActivity : BaseWebViewActivity() {
    private lateinit var notices: Notices

    override val titleRes: Int
        get() = R.string.about_licenses_title

    override fun onCreate(savedInstanceState: Bundle?) {
        notices = savedInstanceState?.getState<State>()?.notices
            ?: NoticesXmlParser.parse(resources.openRawResource(R.raw.licenses))
                .apply { addNotice(LicensesDialog.LICENSES_DIALOG_NOTICE) }
        super.onCreate(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putState(State(notices))
    }

    override fun loadWebView() {
        super.loadWebView()
        val html = createHtml(notices, this)
        binding.privacyWebView.apply {
            scrollIndicatorsCompat = (ViewCompat.SCROLL_INDICATOR_TOP
                    or ViewCompat.SCROLL_INDICATOR_BOTTOM)
            setBackgroundColor(Color.TRANSPARENT)
            settings.setSupportMultipleWindows(true)
            webChromeClient = object : WebChromeClient() {
                override fun onCreateWindow(
                    view: WebView,
                    isDialog: Boolean,
                    isUserGesture: Boolean,
                    resultMsg: Message
                ): Boolean {
                    val data = view.hitTestResult.extra
                    if (data != null) {
                        context.startActivitySafe(Uri.parse(data).createViewIntent())
                    }
                    return false
                }
            }
            loadDataWithBaseURL(null, html, "text/html", StandardCharsets.UTF_8.name(), null)
        }
    }

    @Parcelize
    private class State(val notices: Notices) : ParcelableState
}
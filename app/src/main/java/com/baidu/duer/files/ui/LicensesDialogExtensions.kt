package com.baidu.duer.files.ui

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Message
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import com.baidu.duer.files.R
import com.baidu.duer.files.app.application
import com.baidu.duer.files.compat.scrollIndicatorsCompat
import com.baidu.duer.files.util.createViewIntent
import com.baidu.duer.files.util.getColorByAttr
import com.baidu.duer.files.util.getDimensionPixelSize
import com.baidu.duer.files.util.startActivitySafe
import de.psdev.licensesdialog.model.Notices
import java.nio.charset.StandardCharsets

/**
 * @see de.psdev.licensesdialog.LicensesDialog
 */
fun AlertDialog.Builder.setLicensesView(notices: Notices): AlertDialog.Builder {
    val context = context
    val html = createHtml(notices, context)
    return setView(createView(html, context))
}

fun createHtml(notices: Notices, context: Context): String =
    StringBuilder().apply {
        append("<!DOCTYPE html><html lang=\"en-US\"><head><meta charset=\"utf-8\"><style>")
        append(createStyle(context))
        append("</style></head><body><ul>")
        for (notice in notices.notices) {
            append("<li><div>")
            append(notice.name)
            val url = notice.url
            if (!url.isNullOrEmpty()) {
                append(" (<a href=\"")
                append(url)
                append("\" target=\"_blank\">")
                append(url)
                append("</a>)")
            }
            append("</div><pre>")
            val copyright = notice.copyright
            if (!copyright.isNullOrEmpty()) {
                append(copyright)
                append("<br><br>")
            }
            val license = notice.license
            if (license != null) {
                append(license.getSummaryText(context))
            }
            append("</pre></li>")
        }
        append("</ul></body></html>")
    }.toString()

fun createStyle(context: Context): String {
    val primaryTextColor = context.getColor(R.color.md_grey_black).toCssColor()
    val preformattedTextBackgroundColor =
        context.getColor(R.color.webView_background_color).toCssColor()
    // TODO 跟UI讨论下要不要修改
    val linkTextColor = context.getColorByAttr(android.R.attr.textColorLink).toCssColor()
    val textHighlightColor = context.getColorByAttr(android.R.attr.textColorHighlight).toCssColor()
    val titleMarginLeftAndRight =
        if (application.resources.configuration.orientation
            == Configuration.ORIENTATION_LANDSCAPE
        ) "55px" else "20px"
    val contentMarginLeftAndRight =
        if (application.resources.configuration.orientation
            == Configuration.ORIENTATION_LANDSCAPE
        ) "35px" else "16px"
    return """
        ::selection {
            background: $textHighlightColor;
        }
        body {
            color: $primaryTextColor;
            margin: 0;
            overflow-wrap: break-word;
            -webkit-tap-highlight-color: $textHighlightColor;
        }
        ul {
            list-style-type: none;
            margin: 0;
            padding: 0;
        }
        li {
            padding: 12px;
        }
        div {
            padding: 0 12px;
            font-size: 24px; 
            font-weight: 700;
            margin-bottom: 30px;
            margin-left: $titleMarginLeftAndRight; 
            margin-right: $titleMarginLeftAndRight;
        }
        pre {
            background: $preformattedTextBackgroundColor;
            margin-left: $contentMarginLeftAndRight; 
            margin-right: $contentMarginLeftAndRight;
            padding: 30px;
            white-space: pre-wrap;
            font-size: 20px
        }
        a, a:link, a:visited, a:hover, a:focus, a:active {
            color: $primaryTextColor;
        }
    """.trimIndent()
}

fun Int.toCssColor(): String =
    if (Color.alpha(this) == 0xFF) {
        "#%06X".format(this and 0x00FFFFFF)
    } else {
        "rgba(${Color.red(this)}, ${Color.green(this)}, ${Color.blue(this)}, ${
            Color.alpha(this).toFloat() / 0xFF
        })"
    }

private fun createView(html: String, context: Context): View {
    val webView = WebView(context).apply {
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
    return FrameLayout(context).apply {
        setPaddingRelative(
            0, context.getDimensionPixelSize(R.dimen.abc_dialog_title_divider_material), 0, 0
        )
        addView(webView)
    }
}

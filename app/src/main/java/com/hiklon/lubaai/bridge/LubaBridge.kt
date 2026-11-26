package com.hiklon.lubaai.bridge

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.hiklon.lubaai.BuildConfig
import com.hiklon.lubaai.LubaConfig
import com.hiklon.lubaai.R
import com.hiklon.lubaai.util.ExternalLinks

/**
 * Pont JavaScript exposé sous le nom `LubaNative`.
 * Les actions natives ne s'exécutent que si la page courante est la page embarquée.
 */
class LubaBridge(private val activity: Activity, private val webView: WebView) {

    @JavascriptInterface fun platform(): String = "android"

    @JavascriptInterface fun appVersion(): String = BuildConfig.VERSION_NAME

    @JavascriptInterface fun shareText(text: String) = onTrustedPage {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        activity.startActivity(Intent.createChooser(send, activity.getString(R.string.share_with)))
    }

    @JavascriptInterface fun openExternal(url: String) = onTrustedPage {
        val uri = Uri.parse(url)
        if (uri.scheme in ALLOWED_SCHEMES) ExternalLinks.open(activity, uri)
    }

    private fun onTrustedPage(block: () -> Unit) {
        activity.runOnUiThread {
            if (LubaConfig.isTrustedPage(webView.url)) runCatching { block() }
        }
    }

    companion object {
        const val NAME = "LubaNative"
        private val ALLOWED_SCHEMES = setOf("http", "https", "mailto", "tel")
    }
}
